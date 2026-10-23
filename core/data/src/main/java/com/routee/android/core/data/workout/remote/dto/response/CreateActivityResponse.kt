package com.routee.android.core.data.workout.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateActivityResponse(
    @SerialName("activityId")
    val activityId: Long,
    @SerialName("title")
    val title: String,
)
