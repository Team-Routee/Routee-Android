package com.routee.android.feature.workout.main.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.data.workout.model.WorkoutRecord
import java.time.Instant
import kotlin.math.roundToInt

// 복구 모달에 보여줄 이전 기록
@Immutable
internal data class WorkoutRecoveryUiModel(
    val lastRecordedAt: Instant,
    val distanceMeters: Int,
    val durationSec: Int,
)

// 운동 시간은 꺼져 있던 시간을 빼고 마지막 기록 시각까지
internal fun WorkoutRecord.toRecoveryUiModel(): WorkoutRecoveryUiModel = WorkoutRecoveryUiModel(
    lastRecordedAt = lastRecordedAt,
    distanceMeters = distanceMeters.roundToInt(),
    durationSec = durationSecondsAt(lastRecordedAt),
)
