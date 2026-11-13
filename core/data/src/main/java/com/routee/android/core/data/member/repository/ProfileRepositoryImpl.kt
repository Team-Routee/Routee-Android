package com.routee.android.core.data.member.repository

import androidx.core.net.toFile
import androidx.core.net.toUri
import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.member.exception.MemberException
import com.routee.android.core.data.member.model.MemberProfile
import com.routee.android.core.data.member.remote.dto.request.NicknameUpdateRequest
import com.routee.android.core.data.member.remote.dto.request.ProfileImageUpdateRequest
import com.routee.android.core.data.member.remote.service.MemberService
import com.routee.android.core.data.upload.PresignedImageUploader
import com.routee.android.core.data.upload.remote.dto.request.ImageUrlRequest
import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.network.extension.mapApiException
import com.routee.android.core.network.util.ApiResponseHandler
import java.io.File
import javax.inject.Inject

internal class ProfileRepositoryImpl @Inject constructor(
    private val memberService: MemberService,
    private val uploader: PresignedImageUploader,
    private val apiResponseHandler: ApiResponseHandler,
) : ProfileRepository {

    override suspend fun getProfile(): Result<MemberProfile> =
        apiResponseHandler.safeApiCall { memberService.getMemberProfile() }
            .map { MemberProfile(nickname = it.nickname, profileImageUrl = it.profileImageUrl) }
            .mapApiException(::mapMemberException)

    override suspend fun updateNickname(nickname: String): Result<String> =
        apiResponseHandler.safeApiCall { memberService.updateNickname(NicknameUpdateRequest(nickname)) }
            .map { it.nickname }
            .mapApiException(::mapMemberException)

    override suspend fun updateProfileImage(imageUri: String): Result<String?> {
        val imageFile = suspendRunCatching { imageUri.toUri().toFile() }.getOrElse { return Result.failure(it) }
        return uploadProfileImage(imageFile).mapApiException(::mapMemberException)
    }

    override suspend fun resetProfileImage(): Result<Unit> =
        apiResponseHandler.safeUnitApiCall { memberService.resetProfileImage() }
            .mapApiException(::mapMemberException)

    private suspend fun uploadProfileImage(imageFile: File): Result<String?> {
        val url = apiResponseHandler.safeApiCall {
            memberService.createProfileImageUrl(ImageUrlRequest(imageFile.name))
        }.getOrElse { return Result.failure(it) }

        apiResponseHandler.safeUnitApiCall {
            uploader.upload(url.presignedUrl, imageFile)
        }.exceptionOrNull()?.let { return Result.failure(it) }

        return apiResponseHandler.safeApiCall {
            memberService.updateProfileImage(ProfileImageUpdateRequest(url.objectKey))
        }.map { it.profileImageUrl }
    }

    private fun mapMemberException(exception: ApiException): Throwable =
        if (exception is ApiException.NetworkConnection) {
            MemberException.NetworkUnavailable(exception.message)
        } else {
            exception
        }
}
