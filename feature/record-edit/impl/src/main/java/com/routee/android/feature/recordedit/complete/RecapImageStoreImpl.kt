package com.routee.android.feature.recordedit.complete

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.photo.GalleryImageStore
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
private const val PNG_QUALITY = 100

internal class RecapImageStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val galleryImageStore: GalleryImageStore,
) : RecapImageStore {
    override suspend fun saveWithWatermark(bitmap: Bitmap): Result<String> = suspendRunCatching {
        withContext(Dispatchers.IO) { writeWithWatermark(bitmap) }
    }

    override suspend fun saveToGallery(imagePath: String): Result<Unit> =
        galleryImageStore.save(imagePath, File(imagePath).lastModified())

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
