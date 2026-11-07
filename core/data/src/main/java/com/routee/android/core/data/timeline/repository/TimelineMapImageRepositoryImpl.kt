package com.routee.android.core.data.timeline.repository

import android.content.Context
import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.timeline.remote.service.TimelineMapImageService
import com.routee.android.core.data.upload.PresignedImageUploader
import com.routee.android.core.data.upload.remote.dto.request.ImageUrlRequest
import com.routee.android.core.data.workout.exception.mapWorkoutException
import com.routee.android.core.network.extension.mapApiException
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
    override suspend fun uploadMapImage(activityId: Long, jpeg: ByteArray): Result<String> = suspendRunCatching {
        val imageFile = File(context.cacheDir, "$activityId.jpg")
        try {
            withContext(Dispatchers.IO) { imageFile.writeBytes(jpeg) }
            uploadFile(activityId, imageFile).getOrThrow()
        } finally {
            imageFile.delete()
        }
    }

    private suspend fun uploadFile(activityId: Long, imageFile: File): Result<String> {
        val url = apiResponseHandler.safeApiCall {
            service.createMapImageUrl(activityId, ImageUrlRequest(imageFile.name))
        }.mapApiException(::mapWorkoutException).getOrElse { return Result.failure(it) }

        return apiResponseHandler.safeUnitApiCall {
            uploader.upload(url.presignedUrl, imageFile)
        }.map { url.objectKey }
    }
}
