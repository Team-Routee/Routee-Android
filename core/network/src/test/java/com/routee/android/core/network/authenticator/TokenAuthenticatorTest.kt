package com.routee.android.core.network.authenticator

import com.routee.android.core.network.model.BaseResponse
import com.routee.android.core.network.model.ReissueRequest
import com.routee.android.core.network.model.ReissueResponse
import com.routee.android.core.network.session.AuthSessionManager
import com.routee.android.core.network.token.ReissueService
import com.routee.android.core.network.token.TokenProvider
import com.routee.android.core.network.token.TokenReissueManager
import com.routee.android.core.network.util.ApiResponseHandler
import kotlinx.serialization.json.Json
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TokenAuthenticatorTest {

    /**
     * GIVEN: 만료된 Access Token으로 보낸 요청이 401을 받았을 때
     * WHEN: authenticate가 호출되면
     * THEN: 새 Access Token을 붙인 같은 요청을 반환한다
     */
    @Test
    fun `401을 받으면 새 토큰으로 다시 요청한다`() {
        val authenticator = authenticator()

        val retryRequest = authenticator.authenticate(null, unauthorizedResponse())

        assertEquals("Bearer $NEW_ACCESS_TOKEN", retryRequest?.header("Authorization"))
        assertEquals(REQUEST_URL, retryRequest?.url.toString())
    }

    /**
     * GIVEN: 재발급한 토큰으로 다시 보낸 요청도 401을 받았을 때
     * WHEN: authenticate가 호출되면
     * THEN: 더 시도하지 않고 null을 반환한다
     */
    @Test
    fun `재발급 후에도 401이면 다시 시도하지 않는다`() {
        val authenticator = authenticator()
        val retriedResponse = unauthorizedResponse()
            .newBuilder()
            .priorResponse(unauthorizedResponse())
            .build()

        val retryRequest = authenticator.authenticate(null, retriedResponse)

        assertNull(retryRequest)
    }

    private fun authenticator(): TokenAuthenticator {
        val tokenProvider = FakeTokenProvider()
        return TokenAuthenticator(
            TokenReissueManager(
                reissueService = FakeReissueService(),
                authSessionManager = AuthSessionManager(tokenProvider),
                apiResponseHandler = ApiResponseHandler(Json { ignoreUnknownKeys = true }),
            ),
        )
    }

    private fun unauthorizedResponse(): Response = Response.Builder()
        .request(
            Request.Builder()
                .url(REQUEST_URL)
                .header("Authorization", "Bearer $OLD_ACCESS_TOKEN")
                .build(),
        )
        .protocol(Protocol.HTTP_1_1)
        .code(401)
        .message("Unauthorized")
        .build()

    private class FakeTokenProvider : TokenProvider {
        private var accessToken: String? = OLD_ACCESS_TOKEN
        private var refreshToken: String? = "old-refresh"

        override suspend fun getAccessToken(): String? = accessToken

        override suspend fun getRefreshToken(): String? = refreshToken

        override suspend fun saveTokens(
            accessToken: String,
            refreshToken: String,
        ) {
            this.accessToken = accessToken
            this.refreshToken = refreshToken
        }

        override suspend fun clearTokens() {
            accessToken = null
            refreshToken = null
        }
    }

    private class FakeReissueService : ReissueService {
        override suspend fun reissue(
            authorization: String,
            request: ReissueRequest,
        ): BaseResponse<ReissueResponse> = BaseResponse(
            status = 200,
            code = "TOKEN_REISSUE",
            message = "토큰 재발급에 성공했습니다.",
            data = ReissueResponse(accessToken = NEW_ACCESS_TOKEN, refreshToken = "new-refresh"),
        )
    }

    private companion object {
        const val REQUEST_URL = "http://localhost/api/v1/member/summary"
        const val OLD_ACCESS_TOKEN = "old-access"
        const val NEW_ACCESS_TOKEN = "new-access"
    }
}
