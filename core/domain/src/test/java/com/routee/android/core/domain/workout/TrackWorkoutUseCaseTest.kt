package com.routee.android.core.domain.workout

import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackWorkoutUseCaseTest {
    private val locations = FakeLocationRepository()
    private val tracking = FakeWorkoutTrackingRepository()
    private val trackWorkout = TrackWorkoutUseCase(tracking, locations)

    /**
     * GIVEN: 기록 중일 때
     * WHEN: 위치 기록을 시작하고 위치가 오면
     * THEN: 저장된 기록을 먼저 이어가고 위치를 저장소에 넘긴다
     */
    @Test
    fun `저장된 기록을 이어간 뒤 위치를 저장소에 넘긴다`() = runTest {
        backgroundScope.launch { trackWorkout() }
        runCurrent()

        locations.flow.tryEmit(location())
        runCurrent()

        assertTrue(tracking.isRestored)
        assertEquals(listOf(location()), tracking.added)
    }

    /**
     * GIVEN: 기록 중일 때
     * WHEN: 일시정지했다가 다시 시작하면
     * THEN: 일시정지 동안은 위치를 구독하지 않고 다시 시작하면 다시 구독한다
     */
    @Test
    fun `일시정지 중에는 위치를 구독하지 않는다`() = runTest {
        backgroundScope.launch { trackWorkout() }
        runCurrent()
        assertEquals(1, locations.flow.subscriptionCount.value)

        tracking.pause()
        runCurrent()
        assertEquals(0, locations.flow.subscriptionCount.value)

        tracking.resume()
        runCurrent()
        assertEquals(1, locations.flow.subscriptionCount.value)
    }

    /**
     * GIVEN: 기기에 좌표 저장이 계속 실패할 때
     * WHEN: 위치가 두 번 들어오면
     * THEN: 실패를 두 번 다 서비스에 알리고, 위치는 계속 받는다
     */
    @Test
    fun `경로 좌표 저장이 실패해도 알리고 계속 기록한다`() = runTest {
        tracking.addError = IllegalStateException()
        val failures = mutableListOf<Throwable>()
        backgroundScope.launch { trackWorkout(onTrackPointFailure = failures::add) }
        runCurrent()

        locations.flow.tryEmit(location(seconds = 1))
        runCurrent()
        locations.flow.tryEmit(location(seconds = 2))
        runCurrent()

        assertEquals(2, failures.size)
        assertEquals(1, locations.flow.subscriptionCount.value)
    }

    private fun location(seconds: Long = 1) = DeviceLocation(
        latitude = 37.0,
        longitude = 127.0,
        altitude = 100.0,
        altitudeAccuracyMeters = 5f,
        accuracyMeters = 5f,
        time = START.plusSeconds(seconds),
    )

    private class FakeLocationRepository : LocationRepository {
        val flow = MutableSharedFlow<DeviceLocation>(extraBufferCapacity = 16)

        override fun observeLocations(): Flow<DeviceLocation> = flow
    }

    private class FakeWorkoutTrackingRepository : WorkoutTrackingRepository {
        private val _state = MutableStateFlow(WorkoutTrackingState(record = record()))
        override val state: StateFlow<WorkoutTrackingState> = _state
        var isRestored = false
        var addError: Throwable? = null
        val added = mutableListOf<DeviceLocation>()

        override suspend fun start(record: WorkoutRecord): Result<Unit> = Result.success(Unit)

        override suspend fun pause(): Result<Unit> {
            _state.value = _state.value.copy(record = _state.value.record?.paused(START))
            return Result.success(Unit)
        }

        override suspend fun resume(): Result<Unit> {
            _state.value = _state.value.copy(record = _state.value.record?.resumed(START))
            return Result.success(Unit)
        }

        override suspend fun addTrackPoint(location: DeviceLocation): Result<Unit> {
            addError?.let { return Result.failure(it) }
            added += location
            return Result.success(Unit)
        }

        override suspend fun restore(): Result<Unit> {
            isRestored = true
            return Result.success(Unit)
        }

        private fun record() = WorkoutRecord(
            activityId = 1,
            title = "2026.10.22 기록",
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
    }

    private companion object {
        val START: Instant = Instant.ofEpochMilli(1_000_000)
    }
}
