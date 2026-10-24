package com.routee.android.feature.workout.recording.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.data.workout.model.WorkoutPhoto

// 사진 타임라인 모달에 띄운 사진
@Immutable
internal data class OpenedPhoto(
    val id: String,
    val imageUrl: String,
    val title: String,
)

internal fun WorkoutPhoto.toOpenedPhoto(): OpenedPhoto = OpenedPhoto(id = id, imageUrl = imageUrl(), title = title)
