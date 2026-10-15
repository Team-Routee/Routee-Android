package com.routee.android.core.data.auth.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.routee.android.core.data.auth.exception.AuthException
import com.routee.android.core.data.auth.local.TokenLocalDataSource
import com.routee.android.core.data.auth.model.Agreements
import com.routee.android.core.data.auth.model.LoginResult
import com.routee.android.core.data.auth.model.SocialProvider
import com.routee.android.core.data.auth.remote.dto.request.LoginRequest
import com.routee.android.core.data.auth.remote.dto.request.LogoutRequest
import com.routee.android.core.data.auth.remote.dto.request.RegisterRequest
import com.routee.android.core.data.auth.remote.dto.response.TokenResponse
import com.routee.android.core.data.auth.remote.service.AuthService
import com.routee.android.core.data.auth.remote.service.LogoutService
import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.network.model.BaseResponse
import com.routee.android.core.network.session.AuthSessionManager
import com.routee.android.core.network.util.ApiResponseHandler
import java.io.File
import java.io.IOException
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.HttpException
import retrofit2.Response

class AuthRepositoryImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val agreements = Agreements(
        serviceTerms = true,
        privacyPolicy = true,
        locationServiceTerms = true,
        over14 = true,
        marketingConsent = false,
    )

    /**
     * GIVEN: 가입된 회원의 idToken일 때
     * WHEN: 로그인하면
     * THEN: Success를 반환하고 서버 토큰을 저장한다
     */
    @Test
    fun `가입된 회원이면 토큰을 저장하고 Success를 반환한다`() = runTest {
        val tokenStore = tokenStore()
        val repository = repository(FakeAuthService(onLogin = { tokenResponse() }), tokenStore)

        val result = repository.login(ID_TOKEN, SocialProvider.GOOGLE)

        assertEquals(LoginResult.Success, result.getOrNull())
        assertEquals("access", tokenStore.getAccessToken())
        assertEquals("refresh", tokenStore.getRefreshToken())
    }

    /**
     * GIVEN: 서버가 404(미가입)를 내려줄 때
     * WHEN: 로그인하면
     * THEN: idToken을 담은 NeedRegister를 반환하고 토큰은 저장하지 않는다
     */
    @Test
    fun `미가입 회원이면 NeedRegister를 반환한다`() = runTest {
        val tokenStore = tokenStore()
        val repository =
            repository(FakeAuthService(onLogin = { throw httpException(404, "MEMBER_NOT_FOUND") }), tokenStore)

        val result = repository.login(ID_TOKEN, SocialProvider.GOOGLE)

        assertEquals(LoginResult.NeedRegister(ID_TOKEN, SocialProvider.GOOGLE), result.getOrNull())
        assertNull(tokenStore.getAccessToken())
    }

    /**
     * GIVEN: 서버가 회원 없음 코드가 아닌 404를 내려줄 때
     * WHEN: 로그인하면
     * THEN: 회원가입으로 보내지 않고 NotFound로 실패한다
     */
    @Test
    fun `회원 없음 코드가 아닌 404는 NeedRegister로 처리하지 않는다`() = runTest {
        val repository = repository(FakeAuthService(onLogin = { throw httpException(404, "NOT_FOUND") }))

        val result = repository.login(ID_TOKEN, SocialProvider.GOOGLE)

        assertTrue(result.exceptionOrNull() is ApiException.NotFound)
    }

    /**
     * GIVEN: idToken이 만료되었을 때
     * WHEN: 로그인하면
     * THEN: InvalidIdToken으로 실패한다
     */
    @Test
    fun `만료된 idToken이면 InvalidIdToken으로 실패한다`() = runTest {
        val repository = repository(FakeAuthService(onLogin = { throw httpException(401, "ID_TOKEN_EXPIRED") }))

        val result = repository.login(ID_TOKEN, SocialProvider.GOOGLE)

        assertTrue(result.exceptionOrNull() is AuthException.InvalidIdToken)
    }

    /**
     * GIVEN: 필수 약관에 동의하지 않았을 때
     * WHEN: 회원가입하면
     * THEN: RequiredAgreementNotAccepted로 실패한다
     */
    @Test
    fun `필수 약관 미동의면 RequiredAgreementNotAccepted로 실패한다`() = runTest {
        val repository =
            repository(FakeAuthService(onRegister = { throw httpException(400, "REQUIRED_AGREEMENT_NOT_ACCEPTED") }))

        val result = repository.register(NICKNAME, ID_TOKEN, SocialProvider.GOOGLE, agreements)

        assertTrue(result.exceptionOrNull() is AuthException.RequiredAgreementNotAccepted)
    }

    /**
     * GIVEN: 회원가입 요청이 성공할 때
     * WHEN: 회원가입하면
     * THEN: 로그인까지 이어서 진행하고 토큰을 저장한다
     */
    @Test
    fun `회원가입에 성공하면 로그인까지 이어서 진행하고 토큰을 저장한다`() = runTest {
        val tokenStore = tokenStore()
        val repository = repository(
            FakeAuthService(
                onRegister = {},
                onLogin = { tokenResponse() },
            ),
            tokenStore,
        )

        val result = repository.register(NICKNAME, ID_TOKEN, SocialProvider.GOOGLE, agreements)

        assertTrue(result.isSuccess)
        assertEquals("access", tokenStore.getAccessToken())
        assertEquals("refresh", tokenStore.getRefreshToken())
    }

    /**
     * GIVEN: 이미 가입된 회원일 때
     * WHEN: 회원가입하면
     * THEN: 로그인으로 전환하여 토큰을 저장하고 성공한다
     */
    @Test
    fun `이미 가입된 회원이면 로그인으로 전환하여 토큰을 저장하고 성공한다`() = runTest {
        val tokenStore = tokenStore()
        val repository = repository(
            FakeAuthService(
                onRegister = { throw httpException(409, "ALREADY_REGISTERED_MEMBER") },
                onLogin = { tokenResponse() },
            ),
            tokenStore,
        )

        val result = repository.register(NICKNAME, ID_TOKEN, SocialProvider.GOOGLE, agreements)

        assertTrue(result.isSuccess)
        assertEquals("access", tokenStore.getAccessToken())
        assertEquals("refresh", tokenStore.getRefreshToken())
    }

    /**
     * GIVEN: 인증과 관계없는 서버 에러일 때
     * WHEN: 회원가입하면
     * THEN: ApiException을 그대로 전달한다
     */
    @Test
    fun `인증과 관계없는 에러는 ApiException으로 전달한다`() = runTest {
        val repository = repository(FakeAuthService(onRegister = { throw httpException(500, "INTERNAL_SERVER_ERROR") }))

        val result = repository.register(NICKNAME, ID_TOKEN, SocialProvider.GOOGLE, agreements)

        assertTrue(result.exceptionOrNull() is ApiException.ServerError)
    }

    /**
     * GIVEN: 로그인된 상태일 때
     * WHEN: 로그아웃하면
     * THEN: Refresh Token으로 로그아웃 API를 호출하고 저장된 토큰을 지운다
     */
    @Test
    fun `로그아웃하면 서버에 알리고 토큰을 지운다`() = runTest {
        val tokenStore = loggedInTokenStore()
        val logoutService = FakeLogoutService()
        val repository = repository(FakeAuthService(), tokenStore, logoutService)

        val result = repository.logout()

        assertTrue(result.isSuccess)
        assertEquals("refresh", logoutService.lastRequest?.refreshToken)
        assertNull(tokenStore.getAccessToken())
        assertNull(tokenStore.getRefreshToken())
    }

    /**
     * GIVEN: 로그아웃 API가 실패할 때
     * WHEN: 로그아웃하면
     * THEN: 저장된 토큰은 지우고 성공한다
     */
    @Test
    fun `로그아웃 API가 실패해도 토큰을 지운다`() = runTest {
        val tokenStore = loggedInTokenStore()
        val logoutService = FakeLogoutService(onLogout = { throw IOException() })
        val repository = repository(FakeAuthService(), tokenStore, logoutService)

        val result = repository.logout()

        assertTrue(result.isSuccess)
        assertNull(tokenStore.getAccessToken())
        assertNull(tokenStore.getRefreshToken())
    }

    /**
     * GIVEN: 저장된 Refresh Token이 없을 때
     * WHEN: 로그아웃하면
     * THEN: 로그아웃 API를 호출하지 않고 성공한다
     */
    @Test
    fun `Refresh Token이 없으면 로그아웃 API를 호출하지 않는다`() = runTest {
        val logoutService = FakeLogoutService()
        val repository = repository(FakeAuthService(), tokenStore(), logoutService)

        val result = repository.logout()

        assertTrue(result.isSuccess)
        assertNull(logoutService.lastRequest)
    }

    private suspend fun TestScope.loggedInTokenStore() = tokenStore().apply {
        saveTokens(accessToken = "access", refreshToken = "refresh")
    }

    private fun TestScope.tokenStore() = TokenLocalDataSource(
        PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(temporaryFolder.newFolder(), "token.preferences_pb")
        },
    )

    private fun TestScope.repository(
        authService: AuthService,
        tokenStore: TokenLocalDataSource = tokenStore(),
        logoutService: LogoutService = FakeLogoutService(),
    ) = AuthRepositoryImpl(
        authService = authService,
        logoutService = logoutService,
        tokenLocalDataSource = tokenStore,
        authSessionManager = AuthSessionManager(tokenStore),
        apiResponseHandler = ApiResponseHandler(Json { ignoreUnknownKeys = true }),
    )

    private fun tokenResponse() = BaseResponse(
        status = 200,
        code = "LOGIN_SUCCESS",
        message = "로그인에 성공했습니다.",
        data = TokenResponse(accessToken = "access", refreshToken = "refresh", tokenType = "Bearer"),
    )

    private fun httpException(
        statusCode: Int,
        code: String,
    ): HttpException {
        val body = """{"status":$statusCode,"code":"$code","message":"error"}"""
            .toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(statusCode, body))
    }

    private class FakeAuthService(
        private val onLogin: () -> BaseResponse<TokenResponse> = { error("login not expected") },
        private val onRegister: () -> Unit = { error("register not expected") },
    ) : AuthService {
        override suspend fun login(request: LoginRequest): BaseResponse<TokenResponse> = onLogin()

        override suspend fun register(request: RegisterRequest) = onRegister()
    }

    private class FakeLogoutService(
        private val onLogout: () -> Unit = {},
    ) : LogoutService {
        var lastRequest: LogoutRequest? = null

        override suspend fun logout(request: LogoutRequest) {
            lastRequest = request
            onLogout()
        }
    }

    private companion object {
        const val ID_TOKEN = "id-token"
        const val NICKNAME = "루티"
    }
}
