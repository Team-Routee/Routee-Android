package com.routee.android.core.data.workout.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import com.routee.android.core.data.upload.UPLOAD_JPEG_QUALITY
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 운동 중 찍은 사진 파일, 카메라 원본은 압축 후 지움 */
internal class WorkoutPhotoLocalDataSource(
    private val directory: () -> File,
) {
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : this({ File(context.filesDir, DIRECTORY) })

    /** 카메라가 원본을 쓸 빈 파일 */
    suspend fun createCaptureFile(): File = withContext(Dispatchers.IO) {
        val dir = File(directory(), CAPTURE_DIRECTORY).apply { mkdirs() }
        File.createTempFile(CAPTURE_PREFIX, IMAGE_SUFFIX, dir)
    }

    /** 원본을 긴 변 [MAX_EDGE_PX] 이하, 회전 반영해 JPEG 로 압축. 압축 파일 경로 반환 */
    suspend fun compress(
        activityId: Long,
        photoId: String,
        original: File,
    ): String = withContext(Dispatchers.IO) {
        try {
            val bitmap = decodeUpright(original)
            val dir = File(directory(), activityId.toString()).apply { mkdirs() }
            val target = File(dir, "$photoId$IMAGE_SUFFIX")
            try {
                target.outputStream().use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.JPEG, UPLOAD_JPEG_QUALITY, output))
                }
            } catch (error: Exception) {
                target.delete()
                throw error
            } finally {
                bitmap.recycle()
            }
            target.path
        } finally {
            original.delete()
        }
    }

    suspend fun delete(path: String) {
        withContext(Dispatchers.IO) { File(path).delete() }
    }

    /** [activityId] 사진만 남기고 다른 기록 사진, 남은 원본 삭제 */
    suspend fun deleteOthers(activityId: Long) {
        withContext(Dispatchers.IO) {
            directory().listFiles { file -> file.name != activityId.toString() }?.forEach(File::deleteRecursively)
        }
    }

    /** 모든 사진 삭제 */
    suspend fun clear() {
        withContext(Dispatchers.IO) { directory().deleteRecursively() }
    }

    private fun decodeUpright(file: File): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        check(bounds.outWidth > 0 && bounds.outHeight > 0) { "사진을 읽을 수 없음" }

        var sampleSize = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MAX_EDGE_PX) sampleSize *= 2
        val decoded = checkNotNull(
            BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sampleSize }),
        ) { "사진을 읽을 수 없음" }

        val scale = MAX_EDGE_PX.toFloat() / max(decoded.width, decoded.height)
        val scaled = if (scale < 1f) {
            decoded.scale((decoded.width * scale).roundToInt(), (decoded.height * scale).roundToInt())
                .also { if (it != decoded) decoded.recycle() }
        } else {
            decoded
        }
        val rotation = ExifInterface(file).rotationDegrees
        if (rotation == 0) return scaled
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        return Bitmap.createBitmap(scaled, 0, 0, scaled.width, scaled.height, matrix, true)
            .also { if (it != scaled) scaled.recycle() }
    }

    private companion object {
        const val DIRECTORY = "workout_photo"
        const val CAPTURE_DIRECTORY = "capture"
        const val CAPTURE_PREFIX = "capture_"
        const val IMAGE_SUFFIX = ".jpg"
        const val MAX_EDGE_PX = 2048
    }
}
