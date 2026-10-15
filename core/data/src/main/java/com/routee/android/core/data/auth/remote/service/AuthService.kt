package com.routee.android.core.data.auth.remote.service

import com.routee.android.core.data.auth.remote.dto.request.LoginRequest
import com.routee.android.core.data.auth.remote.dto.request.RegisterRequest
import com.routee.android.core.data.auth.remote.dto.response.TokenResponse
import com.routee.android.core.network.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST

internal interface AuthService {
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): BaseResponse<TokenResponse>

    @POST("api/v1/member/register")
    suspend fun register(
        @Body request: RegisterRequest,
    )
}
