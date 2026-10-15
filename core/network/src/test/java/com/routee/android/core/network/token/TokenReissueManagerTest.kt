package com.routee.android.core.network.token

import com.routee.android.core.network.model.BaseResponse
import com.routee.android.core.network.model.ReissueRequest
import com.routee.android.core.network.model.ReissueResponse
import com.routee.android.core.network.session.AuthSessionManager
import com.routee.android.core.network.util.ApiResponseHandler
import java.io.IOException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class TokenReissueManagerTest {

    /**
     * GIVEN: Access Token이 만료되었을 때
     * WHEN: 재발급하면
     * THEN: 새 Access Token과 Refresh Token을 모두 저장하고 새 Access Token을 반환한다
     */
    @Test
    fun `재발급에 성공하면 두 토큰을 모두 저장한다`() = runTest {
        val tokenProvider = FakeTokenProvider()
        val service = FakeReissueService { reissueResponse() }
        val manager = manager(tokenProvider, service)

        val newAccessToken = manager.reissue(OLD_ACCESS_TOKEN)

        assertEquals(NEW_ACCESS_TOKEN, newAccessToken)
        assertEquals(NEW_ACCESS_TOKEN, tokenProvider.getAccessToken())
        assertEquals(NEW_REFRESH_TOKEN, tokenProvider.getRefreshToken())
        assertEquals("Bearer $OLD_ACCESS_TOKEN", service.lastAuthorization)
        assertEquals(OLD_REFRESH_TOKEN, service.lastRequest?.refreshToken)
    }

    /**
     * GIVEN: 다른 요청이 이미 재발급해 저장된 토큰이 바뀌었을 때
     * WHEN: 이전 토큰으로 재발급하면
     * THEN: API를 호출하지 않고 저장된 토큰을 반환한다
     */
    @Test
    fun `이미 재발급된 토큰이 있으면 API를 호출하지 않는다`() = runTest {
        val tokenProvider = FakeTokenProvider(accessToken = NEW_ACCESS_TOKEN)
        val service = FakeReissueService { error("reissue not expected") }
        val manager = manager(tokenProvider, service)

        val accessToken = manager.reissue(OLD_ACCESS_TOKEN)

        assertEquals(NEW_ACCESS_TOKEN, accessToken)
        assertEquals(0, service.callCount)
    }

    /**
     * GIVEN: 여러 요청이 같은 만료 토큰으로 동시에 401을 받았을 때
     * WHEN: 동시에 재발급하면
     * THEN: API는 한 번만 호출하고 모두 새 토큰을 받는다
     */
    @Test
    fun `동시에 재발급해도 API는 한 번만 호출한다`() = runTest {
        val service = FakeReissueService {
            delay(100)
            reissueResponse()
        }
        val manager = manager(FakeTokenProvider(), service)

        val results = List(5) { async { manager.reissue(OLD_ACCESS_TOKEN) } }.awaitAll()

        assertEquals(1, service.callCount)
        assertEquals(List(5) { NEW_ACCESS_TOKEN }, results)
    }

    /**
     * GIVEN: Refresh Token이 만료되었을 때
     * WHEN: 재발급하면
     * THEN: 토큰을 지우고 세션 만료 이벤트를 보낸다
     */
    @Test
    fun `Refresh Token이 만료되면 토큰을 지우고 세션 만료를 알린다`() = runTest {
        val tokenProvider = FakeTokenProvider()
        val sessionManager = AuthSessionManager(tokenProvider)
        val service = FakeReissueService { throw httpException(401, "INVALID_TOKEN") }
        val manager = manager(tokenProvider, service, sessionManager)

        val accessToken = manager.reissue(OLD_ACCESS_TOKEN)

        assertNull(accessToken)
        assertNull(tokenProvider.getAccessToken())
        assertNull(tokenProvider.getRefreshToken())
        assertEquals(Unit, sessionManager.sessionExpired.first())
    }

    /**
     * GIVEN: 저장된 Refresh Token이 없을 때
     * WHEN: 재발급하면
     * THEN: API를 호출하지 않고 세션 만료를 알린다
     */
    @Test
    fun `Refresh Token이 없으면 세션 만료를 알린다`() = runTest {
        val tokenProvider = FakeTokenProvider(refreshToken = null)
        val sessionManager = AuthSessionManager(tokenProvider)
        val service = FakeReissueService { error("reissue not expected") }
        val manager = manager(tokenProvider, service, sessionManager)

        val accessToken = manager.reissue(OLD_ACCESS_TOKEN)

        assertNull(accessToken)
        assertEquals(0, service.callCount)
        assertEquals(Unit, sessionManager.sessionExpired.first())
    }

    /**
     * GIVEN: 네트워크 연결이 끊겼을 때
     * WHEN: 재발급하면
     * THEN: null을 반환하고 토큰은 지우지 않는다
     */
    @Test
    fun `네트워크 오류면 토큰을 유지한다`() = runTest {
        val tokenProvider = FakeTokenProvider()
        val manager = manager(tokenProvider, FakeReissueService { throw IOException() })

        val accessToken = manager.reissue(OLD_ACCESS_TOKEN)

        assertNull(accessToken)
        assertEquals(OLD_ACCESS_TOKEN, tokenProvider.getAccessToken())
        assertEquals(OLD_REFRESH_TOKEN, tokenProvider.getRefreshToken())
    }

    /**
     * GIVEN: 로그아웃으로 토큰이 이미 지워졌을 때
     * WHEN: 재발급하면
     * THEN: API를 호출하지 않고 null을 반환한다
     */
    @Test
    fun `토큰이 지워진 상태면 재발급하지 않는다`() = runTest {
        val service = FakeReissueService { error("reissue not expected") }
        val manager = manager(FakeTokenProvider(accessToken = null, refreshToken = null), service)

        val accessToken = manager.reissue(OLD_ACCESS_TOKEN)

        assertNull(accessToken)
        assertEquals(0, service.callCount)
    }

    private fun manager(
        tokenProvider: TokenProvider,
        service: ReissueService,
        sessionManager: AuthSessionManager = AuthSessionManager(tokenProvider),
    ) = TokenReissueManager(
        reissueService = service,
        authSessionManager = sessionManager,
        apiResponseHandler = ApiResponseHandler(Json { ignoreUnknownKeys = true }),
    )

    private fun reissueResponse() = BaseResponse(
        status = 200,
        code = "TOKEN_REISSUE",
        message = "토큰 재발급에 성공했습니다.",
        data = ReissueResponse(accessToken = NEW_ACCESS_TOKEN, refreshToken = NEW_REFRESH_TOKEN),
    )

    private fun httpException(
        statusCode: Int,
        code: String,
    ): HttpException {
        val body = """{"status":$statusCode,"code":"$code","message":"error"}"""
            .toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(statusCode, body))
    }

    private class FakeTokenProvider(
        private var accessToken: String? = OLD_ACCESS_TOKEN,
        private var refreshToken: String? = OLD_REFRESH_TOKEN,
    ) : TokenProvider {
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

    private class FakeReissueService(
        private val onReissue: suspend () -> BaseResponse<ReissueResponse>,
    ) : ReissueService {
        var callCount = 0
        var lastAuthorization: String? = null
        var lastRequest: ReissueRequest? = null

        override suspend fun reissue(
            authorization: String,
            request: ReissueRequest,
        ): BaseResponse<ReissueResponse> {
            callCount++
            lastAuthorization = authorization
            lastRequest = request
            return onReissue()
        }
    }

    private companion object {
        const val OLD_ACCESS_TOKEN = "old-access"
        const val OLD_REFRESH_TOKEN = "old-refresh"
        const val NEW_ACCESS_TOKEN = "new-access"
        const val NEW_REFRESH_TOKEN = "new-refresh"
    }
}
