package com.routee.android.core.data.member.repository

import com.routee.android.core.data.member.model.MemberSummary

interface MemberRepository {
    suspend fun getMemberSummary(): Result<MemberSummary>
}
