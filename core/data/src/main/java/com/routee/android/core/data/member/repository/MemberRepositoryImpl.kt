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

    private val cacheLock = Any()
    private var cacheVersion = 0

    @Volatile
    override var cachedSummary: MemberSummary? = null
        private set

    override fun clearCache() = synchronized(cacheLock) {
        cacheVersion++
        cachedSummary = null
    }

    override suspend fun getMemberSummary(): Result<MemberSummary> {
        val version = synchronized(cacheLock) { cacheVersion }
        return apiResponseHandler.safeApiCall { memberService.getMemberSummary() }
            .mapCatching { it.toMemberSummary() }
            .onSuccess { summary ->
                synchronized(cacheLock) {
                    if (version == cacheVersion) cachedSummary = summary
                }
            }
    }

    private fun MemberSummaryResponse.toMemberSummary() = MemberSummary(
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        joinDate = LocalDate.parse(joinDate),
        daysSinceJoining = daysSinceJoining,
        totalActivityCount = totalActivityCount,
    )
}
