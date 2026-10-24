package com.routee.android.feature.setting

import com.routee.android.core.data.member.model.MemberProfile
import com.routee.android.core.data.member.repository.ProfileRepository

internal class FakeProfileRepository(
    var profileResult: Result<MemberProfile> = Result.success(MemberProfile("관악산 날다람쥐", null)),
    var nicknameResult: Result<String>? = null,
    var profileImageResult: Result<String?> = Result.success("https://image/uploaded.jpg"),
    var resetResult: Result<Unit> = Result.success(Unit),
) : ProfileRepository {
    var profileCount = 0
    val updatedNicknames = mutableListOf<String>()
    val uploadedImageUris = mutableListOf<String>()
    var resetCount = 0

    override suspend fun getProfile(): Result<MemberProfile> {
        profileCount++
        return profileResult
    }

    override suspend fun updateNickname(nickname: String): Result<String> {
        updatedNicknames += nickname
        return nicknameResult ?: Result.success(nickname)
    }

    override suspend fun updateProfileImage(imageUri: String): Result<String?> {
        uploadedImageUris += imageUri
        return profileImageResult
    }

    override suspend fun resetProfileImage(): Result<Unit> {
        resetCount++
        return resetResult
    }
}
