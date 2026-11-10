package com.routee.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.data.appnotice.model.AppNotice
import com.routee.android.core.data.appnotice.repository.AppNoticeRepository
import com.routee.android.core.domain.auth.ClearDeviceDataUseCase
import com.routee.android.core.network.session.AuthSessionManager
import com.routee.android.feature.onboarding.api.LoginNavKey
import com.routee.android.feature.workout.api.WorkoutMainNavKey
import com.routee.android.model.MainActivityUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
internal class MainActivityViewModel @Inject constructor(
    private val authSessionManager: AuthSessionManager,
    private val clearDeviceData: ClearDeviceDataUseCase,
    private val appNoticeRepository: AppNoticeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainActivityUiState())
    val uiState = _uiState.asStateFlow()

    val sessionExpired: Flow<Unit> = authSessionManager.sessionExpired
        .onEach { clearDeviceData().onFailure { Timber.e(it, DATA_CLEAR_FAILED) } }

    init {
        loadStart()
    }

    fun dismissAppNotice() = _uiState.update { it.copy(appNotice = null) }

    private fun loadStart() {
        viewModelScope.launch {
            val notice = async { loadAppNotice() }
            val startKey = decideStartKey()
            _uiState.update { it.copy(startKey = startKey, appNotice = notice.await()) }
        }
    }

    // 토큰이 없으면 로그인, 있으면 운동 메인 (진행 중인 기록은 운동 메인이 기록 화면으로 보냄)
    private suspend fun decideStartKey(): NavKey {
        val hasSession = authSessionManager.hasSession()
        // 로그아웃, 탈퇴 직후 삭제가 실패했거나 앱이 종료돼 남은 데이터 정리
        if (!hasSession) clearDeviceData().onFailure { Timber.e(it, DATA_CLEAR_FAILED) }
        return if (hasSession) WorkoutMainNavKey else LoginNavKey()
    }

    private suspend fun loadAppNotice(): AppNotice? =
        appNoticeRepository.getAppNoticeConfig()
            .onFailure { Timber.w(it, APP_NOTICE_LOAD_FAILED) }
            .getOrNull()
            ?.noticeFor(BuildConfig.VERSION_NAME)

    private companion object {
        const val DATA_CLEAR_FAILED = "기기 데이터 삭제 실패"
        const val APP_NOTICE_LOAD_FAILED = "업데이트, 점검 안내 설정 읽기 실패"
    }
}
