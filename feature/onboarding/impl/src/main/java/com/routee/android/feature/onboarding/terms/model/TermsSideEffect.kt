package com.routee.android.feature.onboarding.terms.model

internal sealed interface TermsSideEffect {
    data object NavigateBack : TermsSideEffect

    data class NavigateToNickname(
        val idToken: String,
        val marketingConsent: Boolean,
    ) : TermsSideEffect
}
