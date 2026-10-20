package com.routee.android.core.data.auth.exception

sealed class AuthException(message: String?) : Exception(message) {

    class InvalidIdToken(message: String?) : AuthException(message)

    class RequiredAgreementNotAccepted(message: String?) : AuthException(message)

    class AlreadyRegistered(message: String?) : AuthException(message)

    class NetworkUnavailable(message: String?) : AuthException(message)
}
