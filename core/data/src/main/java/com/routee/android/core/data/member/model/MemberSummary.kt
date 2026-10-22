package com.routee.android.core.data.member.model

import java.time.LocalDate

/**
 * 회원 요약 정보
 *
 * @param nickname           닉네임
 * @param profileImageUrl    프로필 사진 URL. 없으면 null
 * @param joinDate           가입한 날짜
 * @param daysSinceJoining   가입 후 지난 일수
 * @param totalActivityCount 누적 운동 기록 수
 */
data class MemberSummary(
    val nickname: String,
    val profileImageUrl: String?,
    val joinDate: LocalDate,
    val daysSinceJoining: Int,
    val totalActivityCount: Int,
)
