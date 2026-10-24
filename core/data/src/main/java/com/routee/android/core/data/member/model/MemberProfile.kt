package com.routee.android.core.data.member.model

/**
 * 프로필 변경 화면의 회원 프로필
 *
 * @param nickname        닉네임
 * @param profileImageUrl 프로필 사진 URL. 없으면 null
 */
data class MemberProfile(
    val nickname: String,
    val profileImageUrl: String?,
)
