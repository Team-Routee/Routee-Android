package com.routee.android.core.data.timeline.repository

import android.content.Context
import android.graphics.Bitmap
import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.timeline.remote.service.TimelineMapImageService
import com.routee.android.core.data.upload.PresignedImageUploader
import com.routee.android.core.data.upload.remote.dto.request.ImageUrlRequest
import com.routee.android.core.network.util.ApiResponseHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class TimelineMapImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val service: TimelineMapImageService,
    private val uploader: PresignedImageUploader,
    private val apiResponseHandler: ApiResponseHandler,
) : TimelineMapImageRepository {
    override suspend fun uploadMapImage(activityId: Long, bitmap: Bitmap): Result<String> = suspendRunCatching {
        val imageFile = withContext(Dispatchers.IO) {
            File.createTempFile("routee_map_", ".jpg", context.cacheDir).also { file ->
                try {
                    file.outputStream().use { output ->
                        check(bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output))
                    }
                } catch (error: Exception) {
                    file.delete()
                    throw error
                }
            }
        }
        try {
            uploadFile(activityId, imageFile).getOrThrow()
        } finally {
            imageFile.delete()
        }
    }

    override suspend fun uploadMapImage(activityId: Long, imageFile: File): Result<String> =
        uploadFile(activityId, imageFile)

    private suspend fun uploadFile(activityId: Long, imageFile: File): Result<String> {
        val url = apiResponseHandler.safeApiCall {
            service.createMapImageUrl(activityId, ImageUrlRequest(imageFile.name))
        }.getOrElse { return Result.failure(it) }

        return apiResponseHandler.safeUnitApiCall {
            uploader.upload(url.presignedUrl, imageFile)
        }.map { url.objectKey }
    }
}
