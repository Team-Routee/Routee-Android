package com.routee.android.core.data.workout.repository

import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.workout.local.toEntity
import com.routee.android.core.data.workout.local.toModel
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.database.dao.WorkoutDao
import javax.inject.Inject

internal class WorkoutRecordRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
) : WorkoutRecordRepository {
    override suspend fun getRecord(): Result<WorkoutRecord?> = suspendRunCatching { workoutDao.getWorkout()?.toModel() }

    override suspend fun startRecord(record: WorkoutRecord): Result<Unit> = suspendRunCatching {
        workoutDao.replaceWorkout(record.toEntity())
    }

    override suspend fun saveRecord(record: WorkoutRecord): Result<Unit> = suspendRunCatching {
        checkUpdated(workoutDao.updateWorkout(record.toEntity()))
    }

    override suspend fun saveTrackPoint(
        record: WorkoutRecord,
        point: TrackPoint,
    ): Result<Unit> = suspendRunCatching {
        checkUpdated(workoutDao.insertTrackPoint(record.toEntity(), point.toEntity(record.activityId)))
    }

    override suspend fun getTrackPoints(activityId: Long): Result<List<TrackPoint>> = suspendRunCatching {
        workoutDao.getTrackPoints(activityId).map { it.toModel() }
    }

    override suspend fun deleteRecord(activityId: Long): Result<Unit> = suspendRunCatching {
        workoutDao.deleteWorkout(activityId)
    }

    override suspend fun clearRecords(): Result<Unit> = suspendRunCatching {
        workoutDao.deleteWorkouts()
    }

    private fun checkUpdated(updatedRows: Int) {
        check(updatedRows == 1) { "저장된 기록이 아님" }
    }
}
