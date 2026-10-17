package com.routee.android.feature.onboarding.nickname.model

internal sealed interface NicknameSideEffect {
    data object NavigateToHome : NicknameSideEffect
}
