package com.routee.android.feature.onboarding.login.model

import androidx.compose.runtime.Immutable

/**
 * 로그인 화면 상태
 *
 * @param isSessionExpiredDialogVisible  오랫동안 이용하지 않아 다시 로그인해야 한다는 안내 노출 여부
 */
@Immutable
internal data class LoginUiState(
    val isSessionExpiredDialogVisible: Boolean = false,
)
