package com.routee.android.feature.onboarding.login

import com.routee.android.core.data.auth.model.Agreements
import com.routee.android.core.data.auth.model.LoginResult
import com.routee.android.core.data.auth.model.SocialProvider
import com.routee.android.core.data.auth.repository.AuthRepository
import com.routee.android.core.data.connectivity.repository.ConnectivityRepository
import com.routee.android.feature.onboarding.MainDispatcherRule
import com.routee.android.feature.onboarding.api.LoginNavKey
import com.routee.android.feature.onboarding.login.model.LoginSideEffect
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(key: LoginNavKey = LoginNavKey()) =
        LoginViewModel(key, FakeAuthRepository(), FakeConnectivityRepository())

    /**
     * GIVEN: 토큰 재발급까지 실패해 로그인으로 왔을 때
     * WHEN: 로그인 화면에 들어오면
     * THEN: 다시 로그인 안내 다이얼로그를 띄운다
     */
    @Test
    fun `세션이 만료되어 들어오면 안내 다이얼로그를 띄운다`() {
        val viewModel = createViewModel(LoginNavKey(isSessionExpired = true))

        assertTrue(viewModel.uiState.value.isSessionExpiredDialogVisible)
    }

    /**
     * GIVEN: 안내 다이얼로그가 떠 있을 때
     * WHEN: 확인을 누르면
     * THEN: 다이얼로그를 닫는다
     */
    @Test
    fun `확인을 누르면 안내 다이얼로그를 닫는다`() {
        val viewModel = createViewModel(LoginNavKey(isSessionExpired = true))

        viewModel.onSessionExpiredDialogDismiss()

        assertFalse(viewModel.uiState.value.isSessionExpiredDialogVisible)
    }

    /**
     * GIVEN: 일반적으로 로그인 화면에 들어올 때
     * WHEN: 화면에 들어오면
     * THEN: 안내 다이얼로그를 띄우지 않는다
     */
    @Test
    fun `일반 진입이면 안내 다이얼로그를 띄우지 않는다`() {
        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.isSessionExpiredDialogVisible)
    }

    /**
     * GIVEN: 기기가 오프라인이라 구글 로그인 단계에서 실패했을 때
     * WHEN: 결과를 받으면
     * THEN: 네트워크 연결 확인 토스트를 띄운다
     */
    @Test
    fun `오프라인이라 구글 로그인에 실패하면 네트워크 토스트를 띄운다`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIdTokenResult(Result.failure(GoogleLoginException.NetworkUnavailable(IOException())))

        assertEquals(LoginSideEffect.ShowNetworkErrorToast, viewModel.sideEffect.first())
    }

    /**
     * GIVEN: 구글 로그인 단계에서 네트워크가 아닌 이유로 실패했을 때
     * WHEN: 결과를 받으면
     * THEN: 일시적인 오류 토스트를 띄운다
     */
    @Test
    fun `구글 로그인이 그 밖의 이유로 실패하면 일시적인 오류 토스트를 띄운다`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIdTokenResult(Result.failure(GoogleLoginException.Failed()))

        assertEquals(LoginSideEffect.ShowUnknownErrorToast, viewModel.sideEffect.first())
    }

    /**
     * GIVEN: 사용자가 구글 로그인 창을 닫았을 때
     * WHEN: 결과를 받으면
     * THEN: 아무 안내도 띄우지 않는다
     */
    @Test
    fun `사용자가 구글 로그인을 닫으면 토스트를 띄우지 않는다`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIdTokenResult(Result.failure(GoogleLoginException.Cancelled(IOException())))
        viewModel.onIdTokenResult(Result.failure(GoogleLoginException.Failed()))

        assertEquals(LoginSideEffect.ShowUnknownErrorToast, viewModel.sideEffect.first())
    }

    private class FakeConnectivityRepository : ConnectivityRepository {
        override fun isConnected(): Boolean = true
    }

    private class FakeAuthRepository : AuthRepository {
        override suspend fun login(
            idToken: String,
            provider: SocialProvider,
        ): Result<LoginResult> = error("login not expected")

        override suspend fun register(
            nickname: String,
            idToken: String,
            provider: SocialProvider,
            agreements: Agreements,
        ): Result<Unit> = error("register not expected")

        override suspend fun logout(): Result<Unit> = error("logout not expected")

        override suspend fun withdraw(): Result<Unit> = error("withdraw not expected")
    }
}
