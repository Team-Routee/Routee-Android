package com.routee.android.core.data.upload.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ImageUrlResponse(
    @SerialName("presignedUrl") val presignedUrl: String,
    @SerialName("objectKey") val objectKey: String,
)
