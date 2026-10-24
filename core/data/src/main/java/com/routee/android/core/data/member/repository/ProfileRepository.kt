package com.routee.android.core.data.member.repository

import com.routee.android.core.data.member.model.MemberProfile

interface ProfileRepository {
    suspend fun getProfile(): Result<MemberProfile>

    /** 바뀐 닉네임 저장, 저장된 닉네임 반환 */
    suspend fun updateNickname(nickname: String): Result<String>

    /** 자른 사진 주소로 업로드 후 프로필 사진 교체, 새 사진 URL 반환 */
    suspend fun updateProfileImage(imageUri: String): Result<String?>

    /** 기본 프로필 이미지로 교체 */
    suspend fun resetProfileImage(): Result<Unit>
}
