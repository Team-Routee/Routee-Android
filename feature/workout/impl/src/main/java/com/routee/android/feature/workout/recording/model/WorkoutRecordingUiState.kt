package com.routee.android.feature.workout.recording.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.data.workout.model.WorkoutPhoto
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.domain.workout.MAX_WORKOUT_PHOTO_COUNT
import com.routee.android.core.map.model.MapPhoto
import com.routee.android.core.map.model.MapPoint
import com.routee.android.feature.workout.model.toMapPoint
import java.io.File
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal enum class WorkoutMode {
    COUNTDOWN,
    RECORDING,
    PAUSED,
    FINISHING,
    ;

    val isTracking: Boolean get() = this == RECORDING || this == PAUSED
}

internal fun WorkoutStatus.toMode(): WorkoutMode? = when (this) {
    WorkoutStatus.ACTIVITY_IN_PROGRESS -> WorkoutMode.RECORDING
    WorkoutStatus.ACTIVITY_PAUSED -> WorkoutMode.PAUSED
    WorkoutStatus.PENDING_UPLOAD -> null
}

// 기록 등록 실패 모달 종류
internal enum class WorkoutSaveFailure {
    NETWORK,
    INVALID_ROUTE,
}

@Immutable
internal data class WorkoutRecordingUiState(
    val mode: WorkoutMode = WorkoutMode.COUNTDOWN,
    val distanceMeters: Int = 0,
    val durationSec: Int = 0,
    val maxElevationMeters: Int = 0,
    val route: ImmutableList<MapPoint> = persistentListOf(),
    // 삭제 스낵바가 떠 있는 사진은 뺌
    val photos: ImmutableList<MapPhoto> = persistentListOf(),
    val openedPhoto: OpenedPhoto? = null,
    val isPhotoDeletePending: Boolean = false,
    val isCameraUnavailable: Boolean = false,
    val saveFailure: WorkoutSaveFailure? = null,
    // 종료 지도 캡처 요청, 캡처를 받으면 null. 화면이 다시 만들어져도 남아서 다시 캡처
    val captureRoute: ImmutableList<MapPoint>? = null,
) {
    val photoCount: Int get() = photos.size

    val isCameraEnabled: Boolean get() = photoCount < MAX_WORKOUT_PHOTO_COUNT

    // 종료 캡처에는 경로선, 내 위치, 사진 마커를 넣지 않음
    val isMapOverlayVisible: Boolean get() = mode != WorkoutMode.FINISHING
}

internal fun WorkoutPhoto.toMapPhoto(): MapPhoto =
    MapPhoto(id = id, imageUrl = imageUrl(), point = point.toMapPoint())

// 기기 파일이 있으면 file: 주소, 서버에서 불러온 사진은 서버 주소
internal fun WorkoutPhoto.imageUrl(): String = filePath?.let { File(it).toURI().toString() } ?: serverImageUrl.orEmpty()
