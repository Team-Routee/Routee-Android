package com.routee.android.core.data.timeline.remote.service

import com.routee.android.core.data.timeline.remote.dto.request.CreateTimelineRequest
import com.routee.android.core.data.timeline.remote.dto.response.TimelineCreateResponse
import com.routee.android.core.data.upload.remote.dto.request.ImageUrlRequest
import com.routee.android.core.data.upload.remote.dto.response.ImageUrlResponse
import com.routee.android.core.network.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

internal interface TimelinePhotoService {
    @POST("api/v1/activity/{activityId}/image-url")
    suspend fun createImageUrl(
        @Path("activityId") activityId: Long,
        @Body request: ImageUrlRequest,
    ): BaseResponse<ImageUrlResponse>

    @POST("api/v1/activity/{activityId}/timeline")
    suspend fun createTimeline(
        @Path("activityId") activityId: Long,
        @Body request: CreateTimelineRequest,
    ): BaseResponse<TimelineCreateResponse>
}
