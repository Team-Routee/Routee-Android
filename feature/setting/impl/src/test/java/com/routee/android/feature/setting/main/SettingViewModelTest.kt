package com.routee.android.feature.setting.main

import com.routee.android.feature.setting.FakeAuthRepository
import com.routee.android.feature.setting.FakeWorkoutRecordRepository
import com.routee.android.feature.setting.MainDispatcherRule
import com.routee.android.feature.setting.clearDeviceData
import com.routee.android.feature.setting.main.model.SettingSideEffect
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `로그아웃 모달에서 확인을 누르면 로그아웃하고 로그인 화면으로 이동한다`() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = SettingViewModel(repository, clearDeviceData())
        viewModel.onLogoutClick()

        viewModel.onLogoutConfirm()

        assertEquals(1, repository.logoutCount)
        assertFalse(viewModel.uiState.value.isLogoutDialogVisible)
        assertEquals(SettingSideEffect.NavigateToLogin, viewModel.sideEffect.first())
    }

    @Test
    fun `로그아웃하면 기기 운동 기록을 지운다`() = runTest {
        val records = FakeWorkoutRecordRepository()
        val viewModel = SettingViewModel(FakeAuthRepository(), clearDeviceData(records))

        viewModel.onLogoutConfirm()

        assertTrue(records.isCleared)
    }

    @Test
    fun `로그아웃에 실패하면 일시적인 오류 토스트를 띄운다`() = runTest {
        val repository = FakeAuthRepository(logoutResult = Result.failure(IOException()))
        val viewModel = SettingViewModel(repository, clearDeviceData())

        viewModel.onLogoutConfirm()

        assertEquals(SettingSideEffect.ShowUnknownErrorToast, viewModel.sideEffect.first())
    }

    @Test
    fun `로그아웃 중에 확인을 다시 눌러도 한 번만 요청한다`() = runTest {
        val repository = FakeAuthRepository().apply { responseGate = CompletableDeferred() }
        val viewModel = SettingViewModel(repository, clearDeviceData())

        viewModel.onLogoutConfirm()
        viewModel.onLogoutConfirm()

        assertEquals(1, repository.logoutCount)
    }
}
