package com.routee.android.core.data.member.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ProfileImageResponse(
    @SerialName("profileImageUrl")
    val profileImageUrl: String? = null,
)
