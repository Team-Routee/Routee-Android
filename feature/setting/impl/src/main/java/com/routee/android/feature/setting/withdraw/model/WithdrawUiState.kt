package com.routee.android.feature.setting.withdraw.model

import androidx.compose.runtime.Immutable

/**
 * 회원 탈퇴 화면 상태
 *
 * @param isAgreed  동의 체크. true 이면 탈퇴 버튼 활성
 */
@Immutable
internal data class WithdrawUiState(
    val isAgreed: Boolean = false,
)
