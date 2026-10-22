package com.routee.android.core.data.upload.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ImageUrlRequest(@SerialName("fileName") val fileName: String)
