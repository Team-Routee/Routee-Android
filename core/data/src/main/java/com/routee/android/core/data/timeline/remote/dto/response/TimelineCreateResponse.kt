package com.routee.android.core.data.timeline.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TimelineCreateResponse(@SerialName("timelineId") val timelineId: Long)
