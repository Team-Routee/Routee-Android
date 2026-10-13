package com.routee.android.core.network.token

interface TokenProvider {
    suspend fun getAccessToken(): String?
}
