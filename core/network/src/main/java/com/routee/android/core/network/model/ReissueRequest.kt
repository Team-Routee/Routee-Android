package com.routee.android.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ReissueRequest(
    @SerialName("refreshToken")
    val refreshToken: String,
)
