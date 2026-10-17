package com.routee.android.core.network.token

import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.network.constant.BEARER_PREFIX
import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.network.model.ReissueRequest
import com.routee.android.core.network.session.AuthSessionManager
import com.routee.android.core.network.util.ApiResponseHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TokenReissueManager @Inject constructor(
    private val reissueService: ReissueService,
    private val authSessionManager: AuthSessionManager,
    private val apiResponseHandler: ApiResponseHandler,
) {
    /**
     * 새 Access Token을 반환한다. 재발급할 수 없으면 null
     *
     * @param failedAccessToken 401을 받은 요청에 붙어 있던 Access Token. 저장된 값과 다르면 다른 요청이 이미 재발급한 것
     */
    suspend fun reissue(failedAccessToken: String?): String? = authSessionManager.withTokenLock {
        val storedAccessToken = getAccessToken() ?: return@withTokenLock null
        if (storedAccessToken != failedAccessToken) return@withTokenLock storedAccessToken
        val refreshToken = getRefreshToken() ?: return@withTokenLock expireSession()

        apiResponseHandler.safeApiCall {
            reissueService.reissue(
                authorization = "$BEARER_PREFIX$storedAccessToken",
                request = ReissueRequest(refreshToken = refreshToken),
            )
        }.fold(
            onSuccess = { token ->
                suspendRunCatching {
                    saveTokens(accessToken = token.accessToken, refreshToken = token.refreshToken)
                    token.accessToken
                }.getOrNull()
            },
            onFailure = { throwable ->
                if (throwable.isExpiredRefreshToken()) expireSession() else null
            },
        )
    }

    private suspend fun TokenProvider.expireSession(): String? {
        clearTokens()
        authSessionManager.notifySessionExpired()
        return null
    }

    private fun Throwable.isExpiredRefreshToken(): Boolean =
        this is ApiException.Unauthorized || (this is ApiException && serverCode == INVALID_TOKEN)

    private companion object {
        const val INVALID_TOKEN = "INVALID_TOKEN"
    }
}
