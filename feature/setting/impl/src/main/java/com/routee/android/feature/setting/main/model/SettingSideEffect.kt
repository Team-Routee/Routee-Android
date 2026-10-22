package com.routee.android.feature.setting.main.model

internal sealed interface SettingSideEffect {
    data object NavigateToLogin : SettingSideEffect

    data object ShowUnknownErrorToast : SettingSideEffect
}
