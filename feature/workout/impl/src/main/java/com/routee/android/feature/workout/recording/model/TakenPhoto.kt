package com.routee.android.feature.workout.recording.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.data.workout.model.TrackPoint
import java.io.File
import java.time.Instant

// 찍고 위치 입력을 기다리는 사진, 좌표와 시각은 카메라가 돌아왔을 때로 정함
@Immutable
internal data class TakenPhoto(
    val filePath: String,
    val point: TrackPoint,
    val takenAt: Instant,
) {
    val imageUrl: String get() = File(filePath).toURI().toString()
}
