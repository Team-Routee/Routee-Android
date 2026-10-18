package com.routee.android.core.network.token

/**
 * 서버 토큰 저장소
 *
 * 구현체는 core:data에 있고, core:network는 이 인터페이스로 토큰을 읽고 쓴다
 */
interface TokenProvider {
    suspend fun getAccessToken(): String?

    suspend fun getRefreshToken(): String?

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
    )

    suspend fun clearTokens()
}
