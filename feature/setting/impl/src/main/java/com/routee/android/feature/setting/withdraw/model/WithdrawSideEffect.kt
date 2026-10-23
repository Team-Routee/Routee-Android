package com.routee.android.feature.setting.withdraw.model

internal sealed interface WithdrawSideEffect {
    data object NavigateToLogin : WithdrawSideEffect

    data object ShowNetworkErrorToast : WithdrawSideEffect

    data object ShowUnknownErrorToast : WithdrawSideEffect
}
