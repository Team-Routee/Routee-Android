package com.routee.android.core.data.timeline.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
internal data class RoutesCreateRequest(val routes: List<RouteRequest>)

@Serializable
internal data class RouteRequest(val name: String, val sequence: Int)
