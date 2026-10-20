package com.routee.android.feature.onboarding.login

import com.routee.android.core.data.auth.model.Agreements
import com.routee.android.core.data.auth.model.LoginResult
import com.routee.android.core.data.auth.model.SocialProvider
import com.routee.android.core.data.auth.repository.AuthRepository
import com.routee.android.feature.onboarding.api.LoginNavKey
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginViewModelTest {

    /**
     * GIVEN: 토큰 재발급까지 실패해 로그인으로 왔을 때
     * WHEN: 로그인 화면에 들어오면
     * THEN: 다시 로그인 안내 다이얼로그를 띄운다
     */
    @Test
    fun `세션이 만료되어 들어오면 안내 다이얼로그를 띄운다`() {
        val viewModel = LoginViewModel(LoginNavKey(isSessionExpired = true), FakeAuthRepository())

        assertTrue(viewModel.uiState.value.isSessionExpiredDialogVisible)
    }

    /**
     * GIVEN: 안내 다이얼로그가 떠 있을 때
     * WHEN: 확인을 누르면
     * THEN: 다이얼로그를 닫는다
     */
    @Test
    fun `확인을 누르면 안내 다이얼로그를 닫는다`() {
        val viewModel = LoginViewModel(LoginNavKey(isSessionExpired = true), FakeAuthRepository())

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
        val viewModel = LoginViewModel(LoginNavKey(), FakeAuthRepository())

        assertFalse(viewModel.uiState.value.isSessionExpiredDialogVisible)
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
    }
}
