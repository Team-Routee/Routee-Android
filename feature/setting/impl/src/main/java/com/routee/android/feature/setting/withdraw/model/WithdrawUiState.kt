package com.routee.android.feature.setting.withdraw.model

import androidx.compose.runtime.Immutable

/**
 * 회원 탈퇴 화면 상태
 *
 * @param isAgreed      동의 체크. true 이면 탈퇴 버튼 활성
 * @param isWithdrawing 탈퇴 요청 중. true 이면 로딩, 탈퇴 버튼과 뒤로가기 막음
 */
@Immutable
internal data class WithdrawUiState(
    val isAgreed: Boolean = false,
    val isWithdrawing: Boolean = false,
)
