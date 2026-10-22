package com.routee.android.feature.recordedit.complete

import android.graphics.Bitmap
import android.net.Uri

internal class FakeRecapImageStore(
    private val saveToGalleryResult: Result<Unit> = Result.success(Unit),
) : RecapImageStore {
    override suspend fun saveWithWatermark(bitmap: Bitmap): Result<String> = Result.success(IMAGE_PATH)

    override suspend fun saveToGallery(imagePath: String): Result<Unit> = saveToGalleryResult

    override fun shareUri(imagePath: String): Uri = error("shareUri not expected")

    companion object {
        const val IMAGE_PATH = "/cache/recap/recap.png"
    }
}
