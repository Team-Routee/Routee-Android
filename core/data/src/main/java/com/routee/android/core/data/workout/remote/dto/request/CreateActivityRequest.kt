package com.routee.android.core.data.workout.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateActivityRequest(
    @SerialName("activityType")
    val activityType: String,
    @SerialName("startedAt")
    val startedAt: String,
)
