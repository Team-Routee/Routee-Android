package com.routee.android.core.data.workout.repository

import com.routee.android.core.database.dao.WorkoutDao
import com.routee.android.core.database.entity.TrackPointEntity
import com.routee.android.core.database.entity.WorkoutEntity

internal class FakeWorkoutDao : WorkoutDao {
    private var workout: WorkoutEntity? = null
    private val points = mutableListOf<TrackPointEntity>()

    override suspend fun getWorkout(): WorkoutEntity? = workout

    override suspend fun upsertWorkout(workout: WorkoutEntity) {
        this.workout = workout
    }

    override suspend fun insertTrackPoint(point: TrackPointEntity) {
        points += point
    }

    override suspend fun getTrackPoints(workoutId: Long): List<TrackPointEntity> =
        points.filter { it.workoutId == workoutId }.sortedBy { it.pointIndex }

    override suspend fun deleteWorkout(workoutId: Long) {
        if (workout?.id == workoutId) workout = null
        points.removeAll { it.workoutId == workoutId }
    }

    override suspend fun deleteWorkouts() {
        workout = null
        points.clear()
    }
}
