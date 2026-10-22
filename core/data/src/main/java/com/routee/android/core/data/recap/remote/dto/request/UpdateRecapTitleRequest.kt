package com.routee.android.core.data.recap.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UpdateRecapTitleRequest(
    @SerialName("title")
    val title: String,
)
