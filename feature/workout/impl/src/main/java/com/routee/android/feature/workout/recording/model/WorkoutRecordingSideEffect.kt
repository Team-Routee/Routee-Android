package com.routee.android.feature.workout.recording.model

import com.routee.android.core.map.model.MapPoint

internal sealed interface WorkoutRecordingSideEffect {
    data object StartTrackingService : WorkoutRecordingSideEffect

    data object StartFailed : WorkoutRecordingSideEffect

    data object ShowRestoreFailed : WorkoutRecordingSideEffect

    data class NavigateToTimeline(val summary: WorkoutSummary) : WorkoutRecordingSideEffect

    data object NavigateToHome : WorkoutRecordingSideEffect

    data class MoveToLastLocation(val point: MapPoint) : WorkoutRecordingSideEffect

    // 카메라 원본을 [filePath]에 쓰도록 기본 카메라 실행
    data class TakePhoto(val filePath: String) : WorkoutRecordingSideEffect

    data object ShowPhotoNoLocation : WorkoutRecordingSideEffect

    data object ShowPhotoFailed : WorkoutRecordingSideEffect

    data object ShowPhotosMissing : WorkoutRecordingSideEffect

    data class ShowPhotoDeleted(val photoId: String) : WorkoutRecordingSideEffect
}
