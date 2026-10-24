package com.routee.android.core.data.timeline.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UpdateTimelineTitleRequest(@SerialName("title") val title: String)
