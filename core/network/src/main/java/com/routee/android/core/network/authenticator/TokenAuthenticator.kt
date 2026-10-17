package com.routee.android.core.network.authenticator

import com.routee.android.core.network.constant.AUTHORIZATION
import com.routee.android.core.network.constant.BEARER_PREFIX
import com.routee.android.core.network.token.TokenReissueManager
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * 401 응답을 받으면 토큰을 재발급하고 원래 요청을 새 토큰으로 다시 보낸다. 재발급 후에도 401이면 더 시도하지 않는다
 */
internal class TokenAuthenticator @Inject constructor(
    private val tokenReissueManager: TokenReissueManager,
) : Authenticator {
    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        if (response.priorResponse != null) return null

        val failedAccessToken = response.request.header(AUTHORIZATION)?.removePrefix(BEARER_PREFIX)
        val newAccessToken = runBlocking { tokenReissueManager.reissue(failedAccessToken) } ?: return null

        return response.request.newBuilder()
            .header(AUTHORIZATION, "$BEARER_PREFIX$newAccessToken")
            .build()
    }
}
