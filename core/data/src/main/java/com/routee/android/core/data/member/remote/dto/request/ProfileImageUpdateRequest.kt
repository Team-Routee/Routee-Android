package com.routee.android.core.data.member.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ProfileImageUpdateRequest(
    @SerialName("objectKey")
    val objectKey: String,
)
