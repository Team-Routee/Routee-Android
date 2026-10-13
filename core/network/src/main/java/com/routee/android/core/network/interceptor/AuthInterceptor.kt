package com.routee.android.core.network.interceptor

import com.routee.android.core.network.constant.AUTHORIZATION
import com.routee.android.core.network.constant.BEARER_PREFIX
import com.routee.android.core.network.token.TokenProvider
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 저장된 Access Token을 Authorization 헤더에 첨부한다. 토큰이 없으면 헤더 없이 요청한다
 */
internal class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = runBlocking { tokenProvider.getAccessToken() }
            ?: return chain.proceed(chain.request())

        val request = chain.request().newBuilder()
            .header(AUTHORIZATION, "$BEARER_PREFIX$accessToken")
            .build()

        return chain.proceed(request)
    }
}
