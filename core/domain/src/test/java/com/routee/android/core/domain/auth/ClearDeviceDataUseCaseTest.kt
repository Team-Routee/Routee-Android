package com.routee.android.core.domain.auth

import com.routee.android.core.data.image.repository.ImageCacheRepository
import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.repository.WorkoutRecordRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import com.routee.android.core.domain.workout.FakeWorkoutPhotoRepository
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class ClearDeviceDataUseCaseTest {
    private val tracking = FakeTrackingRepository()
    private val records = FakeRecordRepository()
    private val photos = FakeWorkoutPhotoRepository()
    private val imageCache = FakeImageCacheRepository()
    private val clearDeviceData = ClearDeviceDataUseCase(tracking, records, photos, imageCache)

    /**
     * GIVEN: 기기에 운동 기록, 사진, 이미지 캐시가 있을 때
     * WHEN: 삭제하면
     * THEN: 진행 중인 기록, 기기 기록, 사진, 이미지 캐시를 모두 지운다
     */
    @Test
    fun `진행 중인 기록, 기기 기록, 사진, 이미지 캐시를 모두 지운다`() = runTest {
        val result = clearDeviceData()

        assertTrue(result.isSuccess)
        assertTrue(tracking.isCleared)
        assertTrue(records.isCleared)
        assertTrue(photos.isCleared)
        assertTrue(imageCache.isCleared)
    }

    /**
     * GIVEN: 기기 기록 삭제가 실패할 때
     * WHEN: 삭제하면
     * THEN: 사진과 이미지 캐시는 지우고 실패를 돌려준다
     */
    @Test
    fun `기기 기록 삭제가 실패해도 나머지는 지우고 실패를 돌려준다`() = runTest {
        records.error = IOException()

        val result = clearDeviceData()

        assertTrue(result.isFailure)
        assertTrue(photos.isCleared)
        assertTrue(imageCache.isCleared)
    }

    private class FakeTrackingRepository : WorkoutTrackingRepository {
        override val state: StateFlow<WorkoutTrackingState> = MutableStateFlow(WorkoutTrackingState())
        var isCleared = false

        override suspend fun start(
            workout: StartedWorkout,
            type: WorkoutType,
            startedAt: Instant,
        ): Result<Unit> = Result.success(Unit)

        override suspend fun pause(): Result<Unit> = Result.success(Unit)

        override suspend fun resume(): Result<Unit> = Result.success(Unit)

        override suspend fun addTrackPoint(location: DeviceLocation): Result<Unit> = Result.success(Unit)

        override suspend fun restore(): Result<Unit> = Result.success(Unit)

        override suspend fun recover(): Result<Unit> = Result.success(Unit)

        override suspend fun finish(endedAt: Instant?): Result<Unit> = Result.success(Unit)

        override suspend fun clear(): Result<Unit> {
            isCleared = true
            return Result.success(Unit)
        }
    }

    private class FakeRecordRepository : WorkoutRecordRepository {
        var isCleared = false
        var error: Throwable? = null

        override suspend fun getRecord(): Result<WorkoutRecord?> = Result.success(null)

        override suspend fun startRecord(record: WorkoutRecord): Result<Unit> = Result.success(Unit)

        override suspend fun saveRecord(record: WorkoutRecord): Result<Unit> = Result.success(Unit)

        override suspend fun saveTrackPoint(
            record: WorkoutRecord,
            point: TrackPoint,
        ): Result<Unit> = Result.success(Unit)

        override suspend fun getTrackPoints(activityId: Long): Result<List<TrackPoint>> = Result.success(emptyList())

        override suspend fun saveMapImage(
            record: WorkoutRecord,
            jpeg: ByteArray,
        ): Result<WorkoutRecord> = Result.success(record)

        override suspend fun deleteRecord(activityId: Long): Result<Unit> = Result.success(Unit)

        override suspend fun clearRecords(): Result<Unit> {
            error?.let { return Result.failure(it) }
            isCleared = true
            return Result.success(Unit)
        }
    }

    private class FakeImageCacheRepository : ImageCacheRepository {
        var isCleared = false

        override suspend fun clear(): Result<Unit> {
            isCleared = true
            return Result.success(Unit)
        }
    }
}
