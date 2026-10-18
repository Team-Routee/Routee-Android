package com.routee.android.core.data.auth.remote.service

import com.routee.android.core.data.auth.remote.dto.request.LogoutRequest
import retrofit2.http.Body
import retrofit2.http.POST

internal interface LogoutService {
    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest,
    )
}
