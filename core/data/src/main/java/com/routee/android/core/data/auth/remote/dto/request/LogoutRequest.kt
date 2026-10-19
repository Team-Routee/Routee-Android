package com.routee.android.core.data.auth.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LogoutRequest(
    @SerialName("refreshToken")
    val refreshToken: String,
)
