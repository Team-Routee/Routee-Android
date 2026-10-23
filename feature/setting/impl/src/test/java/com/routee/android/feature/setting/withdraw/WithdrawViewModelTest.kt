package com.routee.android.feature.setting.withdraw

import com.routee.android.core.data.auth.exception.AuthException
import com.routee.android.feature.setting.FakeAuthRepository
import com.routee.android.feature.setting.MainDispatcherRule
import com.routee.android.feature.setting.withdraw.model.WithdrawSideEffect
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WithdrawViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `동의하지 않으면 탈퇴를 요청하지 않는다`() {
        val repository = FakeAuthRepository()
        val viewModel = WithdrawViewModel(repository)

        viewModel.onWithdrawClick()

        assertEquals(0, repository.withdrawCount)
    }

    @Test
    fun `동의 후 탈퇴에 성공하면 로그인 화면으로 이동한다`() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = WithdrawViewModel(repository).apply { onAgreeChange(true) }

        viewModel.onWithdrawClick()

        assertEquals(1, repository.withdrawCount)
        assertEquals(WithdrawSideEffect.NavigateToLogin, viewModel.sideEffect.first())
    }

    @Test
    fun `네트워크가 끊겨 탈퇴에 실패하면 네트워크 토스트를 띄운다`() = runTest {
        val repository = FakeAuthRepository(withdrawResult = Result.failure(AuthException.NetworkUnavailable(null)))
        val viewModel = WithdrawViewModel(repository).apply { onAgreeChange(true) }

        viewModel.onWithdrawClick()

        assertEquals(WithdrawSideEffect.ShowNetworkErrorToast, viewModel.sideEffect.first())
    }

    @Test
    fun `그 밖의 이유로 탈퇴에 실패하면 일시적인 오류 토스트를 띄운다`() = runTest {
        val repository = FakeAuthRepository(withdrawResult = Result.failure(IOException()))
        val viewModel = WithdrawViewModel(repository).apply { onAgreeChange(true) }

        viewModel.onWithdrawClick()

        assertEquals(WithdrawSideEffect.ShowUnknownErrorToast, viewModel.sideEffect.first())
    }

    @Test
    fun `탈퇴 요청 중에 다시 눌러도 한 번만 요청한다`() {
        val repository = FakeAuthRepository().apply { responseGate = CompletableDeferred() }
        val viewModel = WithdrawViewModel(repository).apply { onAgreeChange(true) }

        viewModel.onWithdrawClick()
        viewModel.onWithdrawClick()

        assertEquals(1, repository.withdrawCount)
    }
}
