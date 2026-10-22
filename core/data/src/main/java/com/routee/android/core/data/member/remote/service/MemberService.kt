package com.routee.android.core.data.member.remote.service

import com.routee.android.core.data.member.remote.dto.response.MemberSummaryResponse
import com.routee.android.core.network.model.BaseResponse
import retrofit2.http.GET

internal interface MemberService {
    @GET("api/v1/member/summary")
    suspend fun getMemberSummary(): BaseResponse<MemberSummaryResponse>
}
