package com.routee.android.feature.setting.main.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class SettingUiState(
    val isLogoutDialogVisible: Boolean = false,
    // 로그아웃 요청 중, 로딩으로 다른 메뉴와 탭을 막음
    val isLoggingOut: Boolean = false,
)
