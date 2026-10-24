package com.routee.android.feature.timeline.main.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.data.archive.model.ArchivePhoto
import com.routee.android.core.ui.UiState

@Immutable
internal data class TimelineUiState(
    val distance: String,
    val duration: String,
    val maxElevation: String,
    val isTitleEditing: Boolean = false,
    val track: UiState<TimelineTrack> = UiState.Loading,
    val photos: List<ArchivePhoto> = emptyList(),
)
