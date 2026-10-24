package com.routee.android.core.data.member.exception

sealed class MemberException(message: String?) : Exception(message) {

    class NetworkUnavailable(message: String?) : MemberException(message)
}
