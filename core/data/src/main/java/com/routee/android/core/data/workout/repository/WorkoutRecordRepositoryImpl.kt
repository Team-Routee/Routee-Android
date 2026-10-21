package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.workout.local.WorkoutMapImageLocalDataSource
import com.routee.android.core.data.workout.local.toEntity
import com.routee.android.core.data.workout.local.toModel
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.database.dao.WorkoutDao
import javax.inject.Inject

internal class WorkoutRecordRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val mapImageLocalDataSource: WorkoutMapImageLocalDataSource,
) : WorkoutRecordRepository {
    override suspend fun getRecord(): WorkoutRecord? = workoutDao.getWorkout()?.toModel()

    override suspend fun startRecord(record: WorkoutRecord) {
        workoutDao.replaceWorkout(record.toEntity())
        mapImageLocalDataSource.clear()
    }

    override suspend fun saveRecord(record: WorkoutRecord) {
        workoutDao.upsertWorkout(record.toEntity())
    }

    override suspend fun saveTrackPoint(
        record: WorkoutRecord,
        point: TrackPoint,
    ) {
        workoutDao.insertTrackPoint(record.toEntity(), point.toEntity(record.activityId))
    }

    override suspend fun getTrackPoints(activityId: Long): List<TrackPoint> =
        workoutDao.getTrackPoints(activityId).map { it.toModel() }

    override suspend fun saveMapImage(
        record: WorkoutRecord,
        jpeg: ByteArray,
    ): WorkoutRecord {
        val path = mapImageLocalDataSource.save(record.activityId, jpeg)
        val saved = record.copy(mapImagePath = path, mapImageObjectKey = null)
        workoutDao.upsertWorkout(saved.toEntity())
        return saved
    }

    override suspend fun deleteRecord(activityId: Long) {
        workoutDao.deleteWorkout(activityId)
        mapImageLocalDataSource.delete(activityId)
    }

    override suspend fun clearRecords() {
        workoutDao.deleteWorkouts()
        mapImageLocalDataSource.clear()
    }
}
