package com.routee.android.core.domain.workout

import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutPhoto
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddWorkoutPhotoUseCaseTest {
    private val photos = FakeWorkoutPhotoRepository()
    private val original = File("capture.jpg")

    /**
     * GIVEN: 경로 좌표가 쌓인 기록 중일 때
     * WHEN: 사진을 추가하면
     * THEN: 마지막 경로 좌표에 연결해 추가하고 바로 올린다
     */
    @Test
    fun `마지막 경로 좌표에 연결하고 바로 올린다`() = runTest {
        val tracking = FakeTrackingRepository(points = 3)

        val result = addPhoto(tracking)(original)

        assertEquals(AddWorkoutPhotoResult.Added, result)
        assertEquals(3, photos.photos.value.single().point.pointIndex)
        assertEquals(NOW, photos.photos.value.single().takenAt)
        assertEquals(listOf(7L), photos.syncedActivityIds)
    }

    /**
     * GIVEN: 기록 중이지만 경로 좌표가 아직 없을 때
     * WHEN: 사진을 추가하면
     * THEN: 마지막 위치로 첫 경로 좌표를 만들어 연결한다
     */
    @Test
    fun `경로 좌표가 없으면 마지막 위치로 첫 좌표를 만든다`() = runTest {
        val tracking = FakeTrackingRepository(points = 0)

        val result = addPhoto(tracking, lastLocation = LOCATION)(original)

        assertEquals(AddWorkoutPhotoResult.Added, result)
        assertEquals(listOf(LOCATION), tracking.added)
        assertEquals(1, photos.photos.value.single().point.pointIndex)
    }

    /**
     * GIVEN: 경로 좌표도 마지막 위치도 없을 때
     * WHEN: 사진을 추가하면
     * THEN: 사진을 버리고 위치가 없다고 알린다
     */
    @Test
    fun `위치를 알 수 없으면 사진을 버린다`() = runTest {
        val result = addPhoto(FakeTrackingRepository(points = 0))(original)

        assertEquals(AddWorkoutPhotoResult.NoLocation, result)
        assertTrue(photos.photos.value.isEmpty())
        assertEquals(listOf(original), photos.discarded)
    }

    /**
     * GIVEN: 이미 20장을 찍었을 때
     * WHEN: 사진을 추가하면
     * THEN: 사진을 버린다
     */
    @Test
    fun `20장이면 사진을 버린다`() = runTest {
        photos.setPhotos(List(MAX_WORKOUT_PHOTO_COUNT) { photo(it) })

        val result = addPhoto(FakeTrackingRepository(points = 1))(original)

        assertEquals(AddWorkoutPhotoResult.Failed, result)
        assertEquals(MAX_WORKOUT_PHOTO_COUNT, photos.photos.value.size)
        assertEquals(listOf(original), photos.discarded)
    }

    /**
     * GIVEN: 이전 기록 사진이 20장 남아 있을 때
     * WHEN: 새 기록에서 사진을 추가하면
     * THEN: 지금 기록 사진만 세어 추가한다
     */
    @Test
    fun `이전 기록 사진은 세지 않는다`() = runTest {
        photos.setPhotos(List(MAX_WORKOUT_PHOTO_COUNT) { photo(it, activityId = 6) })

        val result = addPhoto(FakeTrackingRepository(points = 1))(original)

        assertEquals(AddWorkoutPhotoResult.Added, result)
    }

    /**
     * GIVEN: 종료해서 업로드를 기다리는 기록일 때
     * WHEN: 사진을 추가하면
     * THEN: 사진을 버린다
     */
    @Test
    fun `기록 중이 아니면 사진을 버린다`() = runTest {
        val tracking = FakeTrackingRepository(points = 1, status = WorkoutStatus.PENDING_UPLOAD)

        val result = addPhoto(tracking)(original)

        assertEquals(AddWorkoutPhotoResult.Failed, result)
        assertEquals(listOf(original), photos.discarded)
    }

    private fun addPhoto(
        tracking: FakeTrackingRepository,
        lastLocation: DeviceLocation? = null,
    ) = AddWorkoutPhotoUseCase(
        trackingRepository = tracking,
        photoRepository = photos,
        locationRepository = FakeLocationRepository(lastLocation),
        clock = Clock.fixed(NOW, ZoneOffset.UTC),
    )

    private class FakeTrackingRepository(
        points: Int,
        status: WorkoutStatus = WorkoutStatus.ACTIVITY_IN_PROGRESS,
    ) : WorkoutTrackingRepository {
        private val _state = MutableStateFlow(
            WorkoutTrackingState(record = RECORD.copy(status = status), trackPoints = (1..points).map(::point)),
        )
        override val state: StateFlow<WorkoutTrackingState> = _state
        val added = mutableListOf<DeviceLocation>()

        override suspend fun start(
            workout: StartedWorkout,
            type: WorkoutType,
            startedAt: Instant,
        ): Result<Unit> = Result.success(Unit)

        override suspend fun pause(): Result<Unit> = Result.success(Unit)

        override suspend fun resume(): Result<Unit> = Result.success(Unit)

        override suspend fun addTrackPoint(location: DeviceLocation): Result<Unit> {
            added += location
            _state.value = _state.value.copy(trackPoints = _state.value.trackPoints + point(1))
            return Result.success(Unit)
        }

        override suspend fun restore(): Result<Unit> = Result.success(Unit)

        override suspend fun finish(): Result<Unit> = Result.success(Unit)

        override suspend fun clear(): Result<Unit> = Result.success(Unit)
    }

    private class FakeLocationRepository(private val lastLocation: DeviceLocation?) : LocationRepository {
        override fun observeLocations(): Flow<DeviceLocation> = emptyFlow()

        override suspend fun getLastLocation(): Result<DeviceLocation?> = Result.success(lastLocation)
    }

    private companion object {
        val START: Instant = Instant.ofEpochMilli(1_000_000)
        val NOW: Instant = START.plusSeconds(60)
        val LOCATION = DeviceLocation(
            latitude = 37.0,
            longitude = 127.0,
            altitude = 90.0,
            altitudeAccuracyMeters = 5f,
            accuracyMeters = 5f,
            time = NOW,
        )
        val RECORD = WorkoutRecord(
            activityId = 7,
            title = "2026.10.24 기록",
            type = WorkoutType.HIKING,
            status = WorkoutStatus.ACTIVITY_IN_PROGRESS,
            startedAt = START,
            durationMillis = 0,
            resumedAt = START,
            pausedAt = null,
            lastRecordedAt = START,
            distanceMeters = 0.0,
            maxElevationMeters = null,
            endedAt = null,
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

        fun photo(
            index: Int,
            activityId: Long = 7,
        ) = WorkoutPhoto(
            id = "photo-$index",
            activityId = activityId,
            filePath = "photo-$index.jpg",
            point = point(1),
            takenAt = START,
        )
    }
}
