package com.routee.android.core.data.member.repository

import com.routee.android.core.data.member.model.MemberSummary
import com.routee.android.core.data.member.remote.dto.response.MemberSummaryResponse
import com.routee.android.core.data.member.remote.service.MemberService
import com.routee.android.core.network.util.ApiResponseHandler
import java.time.LocalDate
import javax.inject.Inject

internal class MemberRepositoryImpl @Inject constructor(
    private val memberService: MemberService,
    private val apiResponseHandler: ApiResponseHandler,
) : MemberRepository {

    override suspend fun getMemberSummary(): Result<MemberSummary> =
        apiResponseHandler.safeApiCall { memberService.getMemberSummary() }
            .mapCatching { it.toMemberSummary() }

    private fun MemberSummaryResponse.toMemberSummary() = MemberSummary(
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        joinDate = LocalDate.parse(joinDate),
        daysSinceJoining = daysSinceJoining,
        totalActivityCount = totalActivityCount,
    )
}
