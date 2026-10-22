package com.routee.android.feature.timeline.main.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class TimelineUiState(
    val distance: String,
    val duration: String,
    val maxElevation: String,
    val isTitleEditing: Boolean = false,
)
