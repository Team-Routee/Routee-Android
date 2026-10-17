package com.routee.android.feature.onboarding.login.model

internal sealed interface LoginSideEffect {
    data object NavigateToHome : LoginSideEffect

    data class NavigateToTerms(val idToken: String) : LoginSideEffect
}
