package com.routee.android.core.data.member.remote.service

import com.routee.android.core.data.member.remote.dto.request.NicknameUpdateRequest
import com.routee.android.core.data.member.remote.dto.request.ProfileImageUpdateRequest
import com.routee.android.core.data.member.remote.dto.response.MemberProfileResponse
import com.routee.android.core.data.member.remote.dto.response.MemberSummaryResponse
import com.routee.android.core.data.member.remote.dto.response.NicknameResponse
import com.routee.android.core.data.member.remote.dto.response.ProfileImageResponse
import com.routee.android.core.data.upload.remote.dto.request.ImageUrlRequest
import com.routee.android.core.data.upload.remote.dto.response.ImageUrlResponse
import com.routee.android.core.network.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

internal interface MemberService {
    @GET("api/v1/member/summary")
    suspend fun getMemberSummary(): BaseResponse<MemberSummaryResponse>

    @GET("api/v1/member/profile")
    suspend fun getMemberProfile(): BaseResponse<MemberProfileResponse>

    @PATCH("api/v1/member/nickname")
    suspend fun updateNickname(@Body request: NicknameUpdateRequest): BaseResponse<NicknameResponse>

    @POST("api/v1/member/profile-image/upload-url")
    suspend fun createProfileImageUrl(@Body request: ImageUrlRequest): BaseResponse<ImageUrlResponse>

    @PATCH("api/v1/member/profile-image")
    suspend fun updateProfileImage(@Body request: ProfileImageUpdateRequest): BaseResponse<ProfileImageResponse>

    @PATCH("api/v1/member/profile-image/default")
    suspend fun resetProfileImage(): BaseResponse<ProfileImageResponse>
}
