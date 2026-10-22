package com.routee.android.core.data.auth.remote.service

import com.routee.android.core.data.auth.remote.dto.request.WithdrawRequest
import retrofit2.http.Body
import retrofit2.http.HTTP

internal interface WithdrawService {
    @HTTP(method = "DELETE", path = "api/v1/member/withdraw", hasBody = true)
    suspend fun withdraw(
        @Body request: WithdrawRequest,
    )
}
