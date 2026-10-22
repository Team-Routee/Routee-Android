package com.routee.android.feature.recordedit.complete

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.designsystem.R as DesignSystemR
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// recap_image
private const val CANVAS_WIDTH = 328f
private const val WATERMARK_WIDTH = 74f
private const val WATERMARK_HEIGHT = 10f
private const val WATERMARK_MARGIN = 20f

private const val RECAP_DIRECTORY = "recap"
private const val GALLERY_DIRECTORY = "Routee"
private const val PNG_QUALITY = 100

internal class RecapImageStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : RecapImageStore {
    override suspend fun saveWithWatermark(bitmap: Bitmap): Result<String> = suspendRunCatching {
        withContext(Dispatchers.IO) { writeWithWatermark(bitmap) }
    }

    override suspend fun saveToGallery(imagePath: String): Result<Unit> = suspendRunCatching {
        withContext(Dispatchers.IO) { copyToGallery(File(imagePath)) }
    }

    override fun shareUri(imagePath: String): Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.recordedit.fileprovider",
        File(imagePath),
    )

    private fun writeWithWatermark(bitmap: Bitmap): String {
        val directory = File(context.cacheDir, RECAP_DIRECTORY).apply {
            deleteRecursively()
            mkdirs()
        }
        val file = File(directory, "recap_${System.currentTimeMillis()}.png")
        file.outputStream().use { bitmap.withWatermark().compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }
        return file.absolutePath
    }

    private fun copyToGallery(source: File) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, source.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$GALLERY_DIRECTORY")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = checkNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        try {
            checkNotNull(resolver.openOutputStream(uri)).use { output ->
                source.inputStream().use { it.copyTo(output) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            }
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
    }

    private fun Bitmap.withWatermark(): Bitmap {
        val result = copy(Bitmap.Config.ARGB_8888, true)
        val scale = result.width / CANVAS_WIDTH
        val watermark = checkNotNull(ContextCompat.getDrawable(context, DesignSystemR.drawable.routee_logo_watermark))
        val right = (result.width - WATERMARK_MARGIN * scale).roundToInt()
        val bottom = (result.height - WATERMARK_MARGIN * scale).roundToInt()
        watermark.setBounds(
            right - (WATERMARK_WIDTH * scale).roundToInt(),
            bottom - (WATERMARK_HEIGHT * scale).roundToInt(),
            right,
            bottom,
        )
        watermark.draw(Canvas(result))
        return result
    }
}
