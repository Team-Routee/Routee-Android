package com.routee.android.core.data.archive.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
internal data class DailyRecordsResponse(
    val date: String,
    val activities: List<ArchiveRecordResponse>,
)

@Serializable
internal data class ArchiveRecordResponse(
    val activityId: Long,
    val title: String,
    val thumbnailUrl: String?,
)
