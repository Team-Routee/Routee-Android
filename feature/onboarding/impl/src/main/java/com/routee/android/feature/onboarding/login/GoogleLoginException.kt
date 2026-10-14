package com.routee.android.feature.onboarding.login

internal sealed class GoogleLoginException(cause: Throwable? = null) : Exception(cause) {

    class Cancelled(cause: Throwable) : GoogleLoginException(cause)

    class NoAccount(cause: Throwable) : GoogleLoginException(cause)

    class Failed(cause: Throwable? = null) : GoogleLoginException(cause)
}
