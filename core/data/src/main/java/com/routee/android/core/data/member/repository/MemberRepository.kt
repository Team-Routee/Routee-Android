package com.routee.android.core.data.member.repository

import com.routee.android.core.data.member.model.MemberSummary

interface MemberRepository {
    /** 마지막으로 조회한 회원 요약, 로그아웃 전까지만 사용 */
    val cachedSummary: MemberSummary?

    /** 회원 요약 캐시 삭제 */
    fun clearCache()

    suspend fun getMemberSummary(): Result<MemberSummary>
}
