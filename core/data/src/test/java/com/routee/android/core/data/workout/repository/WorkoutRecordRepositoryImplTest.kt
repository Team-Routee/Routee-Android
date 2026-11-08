package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutType
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkoutRecordRepositoryImplTest {
    private lateinit var repository: WorkoutRecordRepositoryImpl

    @Before
    fun setUp() {
        repository = WorkoutRecordRepositoryImpl(FakeWorkoutDao())
    }

    /**
     * GIVEN: 기록을 시작했을 때
     * WHEN: 경로 좌표와 갱신된 기록을 저장하면
     * THEN: 경로 좌표와 갱신된 기록을 그대로 읽을 수 있다
     */
    @Test
    fun `경로 좌표와 갱신된 기록을 함께 저장한다`() = runTest {
        repository.startRecord(record())
        val updated = record().copy(distanceMeters = 12.5, lastRecordedAt = Instant.ofEpochMilli(5_000))

        repository.saveTrackPoint(updated, point(0))

        assertEquals(listOf(point(0)), repository.getTrackPoints(ACTIVITY_ID).getOrThrow())
        assertEquals(updated, repository.getRecord().getOrThrow())
    }

    /**
     * GIVEN: 기록을 시작했을 때
     * WHEN: 다른 id의 기록을 저장하면
     * THEN: 실패를 돌려주고 원래 기록은 그대로다
     */
    @Test
    fun `다른 id의 기록은 저장하지 않는다`() = runTest {
        repository.startRecord(record(activityId = 1))

        val result = repository.saveRecord(record(activityId = 2))

        assertTrue(result.isFailure)
        assertEquals(1L, repository.getRecord().getOrThrow()?.activityId)
    }

    private fun record(activityId: Long = ACTIVITY_ID) = WorkoutRecord(
        activityId = activityId,
        title = "2026.10.22 기록",
        type = WorkoutType.HIKING,
        status = WorkoutStatus.ACTIVITY_IN_PROGRESS,
        startedAt = Instant.ofEpochMilli(1_000),
        durationMillis = 0,
        resumedAt = Instant.ofEpochMilli(1_000),
        pausedAt = null,
        lastRecordedAt = Instant.ofEpochMilli(1_000),
        distanceMeters = 0.0,
        maxElevationMeters = null,
        endedAt = null,
    )

    private fun point(index: Int) = TrackPoint(
        pointIndex = index,
        latitude = 37.5,
        longitude = 127.0,
        elevation = 120.0,
        recordedAt = Instant.ofEpochMilli(2_000L + index),
    )

    private companion object {
        const val ACTIVITY_ID = 1L
    }
}
