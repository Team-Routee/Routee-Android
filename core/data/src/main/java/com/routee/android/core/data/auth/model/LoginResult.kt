package com.routee.android.core.data.auth.model

sealed interface LoginResult {
    data object Success : LoginResult

    data class NeedRegister(
        val idToken: String,
        val provider: SocialProvider,
    ) : LoginResult
}
