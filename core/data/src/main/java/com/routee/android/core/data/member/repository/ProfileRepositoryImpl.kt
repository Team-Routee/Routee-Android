package com.routee.android.core.data.member.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.member.exception.MemberException
import com.routee.android.core.data.member.model.MemberProfile
import com.routee.android.core.data.member.remote.dto.request.NicknameUpdateRequest
import com.routee.android.core.data.member.remote.dto.request.ProfileImageUpdateRequest
import com.routee.android.core.data.member.remote.service.MemberService
import com.routee.android.core.data.upload.PresignedImageUploader
import com.routee.android.core.data.upload.UPLOAD_JPEG_QUALITY
import com.routee.android.core.data.upload.remote.dto.request.ImageUrlRequest
import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.network.extension.mapApiException
import com.routee.android.core.network.util.ApiResponseHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val FILE_PREFIX = "routee_profile_"
private const val FILE_SUFFIX = ".jpg"

internal class ProfileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
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
        val imageFile = suspendRunCatching { createUploadFile(imageUri) }.getOrElse { return Result.failure(it) }
        return try {
            uploadProfileImage(imageFile).mapApiException(::mapMemberException)
        } finally {
            withContext(Dispatchers.IO) { imageFile.delete() }
        }
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

    private suspend fun createUploadFile(imageUri: String): File = withContext(Dispatchers.IO) {
        val bitmap = context.contentResolver.openInputStream(Uri.parse(imageUri))
            ?.use(BitmapFactory::decodeStream)
            ?: error("사진을 읽을 수 없음")
        try {
            File.createTempFile(FILE_PREFIX, FILE_SUFFIX, context.cacheDir).also { file ->
                try {
                    file.outputStream().use { output ->
                        check(bitmap.compress(Bitmap.CompressFormat.JPEG, UPLOAD_JPEG_QUALITY, output))
                    }
                } catch (error: Exception) {
                    file.delete()
                    throw error
                }
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun mapMemberException(exception: ApiException): Throwable =
        if (exception is ApiException.NetworkConnection) {
            MemberException.NetworkUnavailable(exception.message)
        } else {
            exception
        }
}
