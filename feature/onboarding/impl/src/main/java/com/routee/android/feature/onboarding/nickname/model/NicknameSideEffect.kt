package com.routee.android.feature.onboarding.nickname.model

internal sealed interface NicknameSideEffect {
    data object NavigateToHome : NicknameSideEffect

    data object ShowNetworkErrorToast : NicknameSideEffect

    data object ShowUnknownErrorToast : NicknameSideEffect
}
