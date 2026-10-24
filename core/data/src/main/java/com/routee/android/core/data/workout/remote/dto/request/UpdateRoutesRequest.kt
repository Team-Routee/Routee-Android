package com.routee.android.core.data.workout.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UpdateRoutesRequest(
    @SerialName("routes")
    val routes: List<RouteRequest>,
)

@Serializable
internal data class RouteRequest(
    @SerialName("name")
    val name: String,
    @SerialName("sequence")
    val sequence: Int,
)
