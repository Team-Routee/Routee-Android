package com.routee.android.core.domain.workout

import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.workout.model.UnfinishedWorkout
import com.routee.android.core.data.workout.repository.WorkoutRecordRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import java.time.Clock
import javax.inject.Inject

/** 앱 진입 때 끝나지 않은 운동 기록, 앱 메모리에 없으면 기기에 저장된 기록을 봄 */
class GetUnfinishedWorkoutUseCase @Inject constructor(
    private val trackingRepository: WorkoutTrackingRepository,
    private val recordRepository: WorkoutRecordRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Result<UnfinishedWorkout> = suspendRunCatching {
        val current = trackingRepository.state.value.record
        val saved = if (current == null) recordRepository.getRecord().getOrThrow() else null
        UnfinishedWorkout.of(current, saved, clock.instant())
    }
}
