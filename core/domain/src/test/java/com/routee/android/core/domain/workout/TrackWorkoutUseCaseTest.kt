package com.routee.android.core.domain.workout

import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.data.workout.model.NextTrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
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
    private val trackWorkout = TrackWorkoutUseCase(tracking, locations, Clock.fixed(START, ZoneOffset.UTC))

    /**
     * GIVEN: 기록 중일 때
     * WHEN: 위치 기록을 시작하고 위치가 오면
     * THEN: 저장된 기록을 먼저 이어가고 위치로 만든 경로 좌표를 저장소에 더한다
     */
    @Test
    fun `저장된 기록을 이어간 뒤 위치를 경로 좌표로 더한다`() = runTest {
        backgroundScope.launch { trackWorkout() }
        runCurrent()

        locations.flow.tryEmit(location())
        runCurrent()

        assertTrue(tracking.isRestored)
        assertEquals(listOf(1), tracking.added.map { it.point.pointIndex })
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

    private fun location() = DeviceLocation(
        latitude = 37.0,
        longitude = 127.0,
        altitude = 100.0,
        altitudeAccuracyMeters = 5f,
        accuracyMeters = 5f,
        time = START.plusSeconds(1),
    )

    private class FakeLocationRepository : LocationRepository {
        val flow = MutableSharedFlow<DeviceLocation>(extraBufferCapacity = 16)

        override fun observeLocations(): Flow<DeviceLocation> = flow
    }

    private class FakeWorkoutTrackingRepository : WorkoutTrackingRepository {
        private val _state = MutableStateFlow(WorkoutTrackingState(record = record()))
        override val state: StateFlow<WorkoutTrackingState> = _state
        var isRestored = false
        val added = mutableListOf<NextTrackPoint>()

        override suspend fun start(record: WorkoutRecord): Result<Unit> = Result.success(Unit)

        override suspend fun pause(): Result<Unit> {
            _state.value = _state.value.copy(record = _state.value.record?.paused(START))
            return Result.success(Unit)
        }

        override suspend fun resume(): Result<Unit> {
            _state.value = _state.value.copy(record = _state.value.record?.resumed(START))
            return Result.success(Unit)
        }

        override suspend fun addTrackPoint(next: NextTrackPoint): Result<Unit> {
            added += next
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
