package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutType
import java.time.Instant

internal class FakeWorkoutRemoteRepository(
    private val updateStatusError: Throwable? = null,
) : WorkoutRemoteRepository {
    val updatedStatuses = mutableListOf<Pair<Long, WorkoutStatus>>()

    override suspend fun createWorkout(
        type: WorkoutType,
        startedAt: Instant,
    ): Result<StartedWorkout> = Result.success(StartedWorkout(activityId = 1, title = "2026.10.22 기록"))

    override suspend fun updateStatus(
        activityId: Long,
        status: WorkoutStatus,
    ): Result<Unit> {
        updatedStatuses += activityId to status
        return updateStatusError?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    override suspend fun completeWorkout(
        record: WorkoutRecord,
        trackPoints: List<TrackPoint>,
        mapImageObjectKey: String,
    ): Result<Unit> = Result.success(Unit)
}
