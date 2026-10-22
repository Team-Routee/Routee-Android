package com.routee.android.feature.archive.detail.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.data.archive.model.ArchivePhoto
import com.routee.android.core.map.model.MapPhoto
import com.routee.android.core.map.model.MapPoint
import com.routee.android.core.ui.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class ArchiveDetailUiState(
    val record: UiState<ArchiveDetailRecord> = UiState.Loading,
    val isTitleEditing: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
)

@Immutable
internal data class ArchiveDetailRecord(
    val title: String,
    val date: String,
    val distance: String,
    val duration: String,
    val maxElevation: String,
    val track: ImmutableList<MapPoint> = persistentListOf(),
    val markers: ImmutableList<MapPhoto> = persistentListOf(),
    val photos: ImmutableList<ArchivePhoto> = persistentListOf(),
    val routeNames: ImmutableList<String> = persistentListOf(),
)
