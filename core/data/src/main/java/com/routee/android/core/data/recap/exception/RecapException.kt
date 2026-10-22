package com.routee.android.core.data.recap.exception

sealed class RecapException(message: String?) : Exception(message) {

    class NetworkUnavailable(message: String?) : RecapException(message)
}
