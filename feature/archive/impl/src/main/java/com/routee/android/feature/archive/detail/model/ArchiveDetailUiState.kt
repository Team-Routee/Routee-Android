package com.routee.android.feature.archive.detail.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.designsystem.component.RouteeTimelinePhoto
import com.routee.android.core.map.model.MapMarker
import com.routee.android.core.map.model.MapPoint
import com.routee.android.core.ui.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class ArchiveDetailUiState(
    val record: UiState<ArchiveDetailRecord> = UiState.Loading,
)

@Immutable
internal data class ArchiveDetailRecord(
    val title: String,
    val date: String,
    val distance: String,
    val duration: String,
    val maxElevation: String,
    val track: ImmutableList<MapPoint> = persistentListOf(),
    val markers: ImmutableList<MapMarker> = persistentListOf(),
    val photos: ImmutableList<RouteeTimelinePhoto> = persistentListOf(),
    val routeNames: ImmutableList<String> = persistentListOf(),
)
