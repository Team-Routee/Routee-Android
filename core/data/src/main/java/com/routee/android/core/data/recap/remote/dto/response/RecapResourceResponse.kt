package com.routee.android.core.data.recap.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RecapResourceResponse(
    @SerialName("distance")
    val distance: Int,
    @SerialName("durationSec")
    val durationSec: Int,
    @SerialName("maxElevation")
    val maxElevation: Int,
    @SerialName("mapImageUrl")
    val mapImageUrl: String? = null,
    @SerialName("routes")
    val routes: List<RecapRouteResponse> = emptyList(),
)

@Serializable
internal data class RecapRouteResponse(
    @SerialName("sequence")
    val sequence: Int,
    @SerialName("name")
    val name: String,
)
