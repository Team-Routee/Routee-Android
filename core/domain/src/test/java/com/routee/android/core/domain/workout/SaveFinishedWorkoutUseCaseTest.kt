package com.routee.android.core.domain.workout

import android.graphics.Bitmap
import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.timeline.repository.TimelineMapImageRepository
import com.routee.android.core.data.workout.exception.WorkoutException
import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.repository.WorkoutRecordRepository
import com.routee.android.core.data.workout.repository.WorkoutRemoteRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import java.io.File
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveFinishedWorkoutUseCaseTest {
    private val tracking = FakeTrackingRepository()
    private val records = FakeRecordRepository()
    private val mapImages = FakeMapImageRepository()
    private val remote = FakeRemoteRepository()
    private val photos = FakeWorkoutPhotoRepository()
    private val saveFinishedWorkout = SaveFinishedWorkoutUseCase(tracking, records, mapImages, remote, photos)

    /**
     * GIVEN: 경로 좌표가 2개 이상인 종료한 기록일 때
     * WHEN: 지도 캡처와 함께 저장하면
     * THEN: 캡처를 기기에 저장해 올리고, 받은 키로 완료한 뒤 기기 기록을 지운다
     */
    @Test
    fun `저장하면 캡처를 올리고 완료한 뒤 기기 기록을 지운다`() = runTest {
        tracking.state(points = 2)

        val result = saveFinishedWorkout(JPEG)

        assertEquals(WorkoutSaveResult.Saved, result)
        assertEquals("map/7.jpg", mapImages.uploadedPath)
        assertEquals("map-key", records.saved?.mapImageObjectKey)
        assertEquals("map-key", remote.completedKey)
        assertTrue(tracking.isCleared)
    }

    /**
     * GIVEN: 찍을 때 못 올린 사진이 있는 종료한 기록일 때
     * WHEN: 저장하면
     * THEN: 완료 전에 사진을 다시 올리고, 저장 후 기기 사진을 지운다
     */
    @Test
    fun `저장하면 못 올린 사진을 다시 올리고 기기 사진을 지운다`() = runTest {
        tracking.state(points = 2)

        val result = saveFinishedWorkout(JPEG)

        assertEquals(WorkoutSaveResult.Saved, result)
        assertEquals(listOf(7L), photos.syncedActivityIds)
        assertTrue(photos.isCleared)
    }

    /**
     * GIVEN: 다시 올려도 사진 1장이 실패할 때
     * WHEN: 저장하면
     * THEN: 그 사진만 빼고 완료하고 빠진 사진이 있다고 알린다
     */
    @Test
    fun `다시 올려도 실패한 사진은 빼고 저장한다`() = runTest {
        val photos = FakeWorkoutPhotoRepository(missingCount = 1)
        val saveFinishedWorkout = SaveFinishedWorkoutUseCase(tracking, records, mapImages, remote, photos)
        tracking.state(points = 2)

        val result = saveFinishedWorkout(JPEG)

        assertEquals(WorkoutSaveResult.PhotosMissing, result)
        assertEquals("map-key", remote.completedKey)
        assertTrue(photos.isCleared)
    }

    /**
     * GIVEN: 경로 좌표가 1개뿐인 기록일 때
     * WHEN: 저장하면
     * THEN: 서버에 보내지 않고 기기 기록을 지운다
     */
    @Test
    fun `경로 좌표가 2개 미만이면 저장하지 않고 기기 기록을 지운다`() = runTest {
        tracking.state(points = 1)

        val result = saveFinishedWorkout(JPEG)

        assertEquals(WorkoutSaveResult.InvalidRoute, result)
        assertNull(remote.completedKey)
        assertTrue(tracking.isCleared)
    }

    /**
     * GIVEN: 경로 좌표는 있지만 운동 시간이 1초 미만일 때
     * WHEN: 저장하면
     * THEN: 서버에 보내지 않고 기기 기록을 지운다
     */
    @Test
    fun `운동 시간이 1초 미만이면 저장하지 않고 기기 기록을 지운다`() = runTest {
        tracking.state(points = 2, record = RECORD.copy(durationMillis = 800))

        val result = saveFinishedWorkout(JPEG)

        assertEquals(WorkoutSaveResult.InvalidRoute, result)
        assertNull(remote.completedKey)
        assertTrue(tracking.isCleared)
    }

    /**
     * GIVEN: 지도 업로드가 실패할 때
     * WHEN: 저장하면
     * THEN: 완료하지 않고 기기 기록은 다시 보낼 수 있게 남긴다
     */
    @Test
    fun `업로드가 실패하면 기기 기록을 남긴다`() = runTest {
        tracking.state(points = 2)
        mapImages.error = IOException()

        val result = saveFinishedWorkout(JPEG)

        assertEquals(WorkoutSaveResult.Failed, result)
        assertNull(remote.completedKey)
        assertFalse(tracking.isCleared)
        assertFalse(photos.isCleared)
    }

    /**
     * GIVEN: 지도 캡처에 실패했고 저장해 둔 캡처도 없을 때
     * WHEN: 저장하면
     * THEN: 다시 캡처하라고 돌려주고 기기 기록은 남긴다
     */
    @Test
    fun `캡처가 없으면 다시 캡처하라고 하고 기기 기록을 남긴다`() = runTest {
        tracking.state(points = 2)

        val result = saveFinishedWorkout(null)

        assertEquals(WorkoutSaveResult.MapImageMissing, result)
        assertNull(remote.completedKey)
        assertFalse(tracking.isCleared)
    }

    /**
     * GIVEN: 앱이 다시 켜져 상태는 비었고 기기에 지도 키를 올려 둔 업로드 대기 기록이 있을 때
     * WHEN: 캡처 없이 저장하면
     * THEN: 기기 기록을 불러와 지도는 다시 올리지 않고 그 키로 완료한 뒤 지운다
     */
    @Test
    fun `저장해 둔 지도 키가 있으면 다시 올리지 않고 완료한다`() = runTest {
        tracking.pending = pendingState(RECORD.copy(mapImageObjectKey = "saved-key", mapImagePath = "map/7.jpg"))

        val result = saveFinishedWorkout(null)

        assertEquals(WorkoutSaveResult.Saved, result)
        assertNull(mapImages.uploadedPath)
        assertEquals("saved-key", remote.completedKey)
        assertTrue(tracking.isCleared)
    }

    /**
     * GIVEN: 지도 키는 없고 캡처 파일만 남은 업로드 대기 기록일 때
     * WHEN: 캡처 없이 저장하면
     * THEN: 남은 캡처 파일을 올려 완료한다
     */
    @Test
    fun `저장해 둔 캡처 파일이 있으면 그 파일을 올려 완료한다`() = runTest {
        tracking.pending = pendingState(RECORD.copy(mapImagePath = "map/7.jpg"))

        val result = saveFinishedWorkout(null)

        assertEquals(WorkoutSaveResult.Saved, result)
        assertEquals("map/7.jpg", mapImages.uploadedPath)
        assertEquals("map-key", remote.completedKey)
    }

    /**
     * GIVEN: 서버에 기록이 없어 완료를 거절할 때
     * WHEN: 저장하면
     * THEN: 다시 보내도 안 되니 기기 기록과 사진을 지운다
     */
    @Test
    fun `완료를 거절하면 기기 기록을 지운다`() = runTest {
        tracking.state(points = 2)
        remote.error = WorkoutException.Rejected("ACTIVITY_NOT_FOUND")

        val result = saveFinishedWorkout(JPEG)

        assertEquals(WorkoutSaveResult.Rejected, result)
        assertTrue(tracking.isCleared)
        assertTrue(photos.isCleared)
    }

    /**
     * GIVEN: 서버에 기록이 없어 지도 업로드 주소를 주지 않을 때
     * WHEN: 저장하면
     * THEN: 완료하지 않고 기기 기록을 지운다
     */
    @Test
    fun `지도 업로드를 거절하면 기기 기록을 지운다`() = runTest {
        tracking.state(points = 2)
        mapImages.error = WorkoutException.Rejected("ACTIVITY_NOT_FOUND")

        val result = saveFinishedWorkout(JPEG)

        assertEquals(WorkoutSaveResult.Rejected, result)
        assertNull(remote.completedKey)
        assertTrue(tracking.isCleared)
    }

    private fun pendingState(record: WorkoutRecord) =
        WorkoutTrackingState(record = record, trackPoints = (1..2).map(::point))

    private class FakeTrackingRepository : WorkoutTrackingRepository {
        private val _state = MutableStateFlow(WorkoutTrackingState())
        override val state: StateFlow<WorkoutTrackingState> = _state
        var isCleared = false

        // 기기에 남은 업로드 대기 기록, 불러오면 상태가 됨
        var pending: WorkoutTrackingState? = null

        fun state(
            points: Int,
            record: WorkoutRecord = RECORD,
        ) {
            _state.value = WorkoutTrackingState(record = record, trackPoints = (1..points).map(::point))
        }

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

        override suspend fun loadPendingUpload(): Result<Unit> {
            pending?.let { _state.value = it }
            return Result.success(Unit)
        }

        override suspend fun finish(endedAt: Instant?): Result<Unit> = Result.success(Unit)

        override suspend fun clear(): Result<Unit> {
            isCleared = true
            _state.value = WorkoutTrackingState()
            return Result.success(Unit)
        }
    }

    private class FakeRecordRepository : WorkoutRecordRepository {
        var saved: WorkoutRecord? = null

        override suspend fun getRecord(): Result<WorkoutRecord?> = Result.success(saved)

        override suspend fun startRecord(record: WorkoutRecord): Result<Unit> = Result.success(Unit)

        override suspend fun saveRecord(record: WorkoutRecord): Result<Unit> {
            saved = record
            return Result.success(Unit)
        }

        override suspend fun saveTrackPoint(
            record: WorkoutRecord,
            point: TrackPoint,
        ): Result<Unit> = Result.success(Unit)

        override suspend fun getTrackPoints(activityId: Long): Result<List<TrackPoint>> = Result.success(emptyList())

        override suspend fun saveMapImage(
            record: WorkoutRecord,
            jpeg: ByteArray,
        ): Result<WorkoutRecord> = Result.success(record.copy(mapImagePath = "map/${record.activityId}.jpg"))

        override suspend fun deleteRecord(activityId: Long): Result<Unit> = Result.success(Unit)

        override suspend fun clearRecords(): Result<Unit> = Result.success(Unit)
    }

    private class FakeMapImageRepository : TimelineMapImageRepository {
        var uploadedPath: String? = null
        var error: Throwable? = null

        override suspend fun uploadMapImage(
            activityId: Long,
            bitmap: Bitmap,
        ): Result<String> = Result.failure(UnsupportedOperationException())

        override suspend fun uploadMapImage(
            activityId: Long,
            imageFile: File,
        ): Result<String> {
            error?.let { return Result.failure(it) }
            uploadedPath = imageFile.path
            return Result.success("map-key")
        }
    }

    private class FakeRemoteRepository : WorkoutRemoteRepository {
        var completedKey: String? = null
        var error: Throwable? = null

        override suspend fun createWorkout(
            type: WorkoutType,
            startedAt: Instant,
        ): Result<StartedWorkout> = Result.success(StartedWorkout(activityId = 7, title = "2026.10.23 기록"))

        override suspend fun updateStatus(
            activityId: Long,
            status: WorkoutStatus,
        ): Result<Unit> = Result.success(Unit)

        override suspend fun completeWorkout(
            record: WorkoutRecord,
            trackPoints: List<TrackPoint>,
            mapImageObjectKey: String,
        ): Result<Unit> {
            error?.let { return Result.failure(it) }
            completedKey = mapImageObjectKey
            return Result.success(Unit)
        }

        override suspend fun updateRoutes(
            activityId: Long,
            names: List<String>,
        ): Result<Unit> = Result.success(Unit)
    }

    private companion object {
        val START: Instant = Instant.ofEpochMilli(1_000_000)
        val JPEG = byteArrayOf(1, 2, 3)
        val RECORD = WorkoutRecord(
            activityId = 7,
            title = "2026.10.23 기록",
            type = WorkoutType.HIKING,
            status = WorkoutStatus.PENDING_UPLOAD,
            startedAt = START,
            durationMillis = 60_000,
            resumedAt = null,
            pausedAt = null,
            lastRecordedAt = START,
            distanceMeters = 100.0,
            maxElevationMeters = null,
            endedAt = START.plusSeconds(60),
            mapImageObjectKey = null,
            mapImagePath = null,
        )

        fun point(index: Int) = TrackPoint(
            pointIndex = index,
            latitude = 37.0,
            longitude = 127.0,
            elevation = 90.0,
            recordedAt = START,
        )
    }
}
