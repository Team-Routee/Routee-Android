package com.routee.android.feature.setting.withdraw.model

internal sealed interface WithdrawSideEffect {
    data object NavigateToLogin : WithdrawSideEffect
}
