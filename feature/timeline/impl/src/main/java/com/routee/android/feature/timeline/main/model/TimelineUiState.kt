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
    val photos: UiState<List<ArchivePhoto>> = UiState.Loading,
) {
    // 불러오는 중이거나 사진이 있으면 사진 섹션 표시
    val hasPhotoSection: Boolean
        get() = photos is UiState.Loading || (photos as? UiState.Success)?.data?.isNotEmpty() == true
}
