package com.routee.android.core.data.timeline.remote.service

import com.routee.android.core.data.upload.remote.dto.request.ImageUrlRequest
import com.routee.android.core.data.upload.remote.dto.response.ImageUrlResponse
import com.routee.android.core.network.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

internal interface TimelineMapImageService {
    @POST("api/v1/activity/{activityId}/map-image-url")
    suspend fun createMapImageUrl(
        @Path("activityId") activityId: Long,
        @Body request: ImageUrlRequest,
    ): BaseResponse<ImageUrlResponse>
}
