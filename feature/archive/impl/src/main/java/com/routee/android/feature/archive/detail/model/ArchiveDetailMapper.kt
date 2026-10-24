package com.routee.android.feature.archive.detail.model

import com.routee.android.core.data.archive.model.ArchiveDetail
import com.routee.android.core.map.model.MapPhoto
import com.routee.android.core.map.model.MapPoint
import com.routee.android.core.ui.format.formatDistanceKm
import com.routee.android.core.ui.format.formatDurationClock
import java.time.format.DateTimeFormatter
import kotlinx.collections.immutable.toImmutableList

private val RecordDateFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd")

internal fun ArchiveDetail.toArchiveDetailRecord() = ArchiveDetailRecord(
    title = title,
    date = date.format(RecordDateFormat),
    distance = formatDistanceKm(distanceMeters),
    duration = formatDurationClock(durationSeconds),
    maxElevation = maxElevationMeters.toString(),
    track = track.trackPoints.sortedBy { it.pointIndex }.map { MapPoint(it.latitude, it.longitude) }.toImmutableList(),
    markers = track.timelineMarkers.map {
        MapPhoto(it.timelineId.toString(), it.thumbnailUrl, MapPoint(it.latitude, it.longitude))
    }.toImmutableList(),
    photos = photos.toImmutableList(),
    routeNames = routes.map { it.name }.toImmutableList(),
)
