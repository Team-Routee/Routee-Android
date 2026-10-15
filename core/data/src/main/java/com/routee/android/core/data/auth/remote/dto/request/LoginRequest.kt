package com.routee.android.core.data.auth.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LoginRequest(
    @SerialName("idToken")
    val idToken: String,
    @SerialName("provider")
    val provider: String,
)
