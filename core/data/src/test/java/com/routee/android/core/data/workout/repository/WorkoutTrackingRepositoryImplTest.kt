package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.workout.local.WorkoutMapImageLocalDataSource
import com.routee.android.core.data.workout.model.NextTrackPoint
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutType
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WorkoutTrackingRepositoryImplTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private val clock = TestClock(START)
    private lateinit var recordRepository: WorkoutRecordRepository
    private lateinit var repository: WorkoutTrackingRepositoryImpl

    @Before
    fun setUp() {
        recordRepository =
            WorkoutRecordRepositoryImpl(FakeWorkoutDao(), WorkoutMapImageLocalDataSource { tempFolder.root })
        repository = WorkoutTrackingRepositoryImpl(recordRepository, clock)
    }

    /**
     * GIVEN: 기기에 경로 좌표 2개가 저장된 기록이 있을 때
     * WHEN: 이어가면
     * THEN: 저장된 기록과 경로 좌표를 불러온다
     */
    @Test
    fun `저장된 기록을 불러와 이어간다`() = runTest {
        recordRepository.startRecord(record())
        recordRepository.saveTrackPoint(record(), point(1))
        recordRepository.saveTrackPoint(record(), point(2))

        repository.restore()

        assertEquals(listOf(1, 2), repository.state.value.trackPoints.map { it.pointIndex })
        assertEquals(1L, repository.state.value.record?.activityId)
    }

    /**
     * GIVEN: 종료를 눌러 저장 확인 전인 기록이 있을 때
     * WHEN: 이어가려 하면
     * THEN: 이어가지 않는다
     */
    @Test
    fun `저장 확인 전인 기록은 이어가지 않는다`() = runTest {
        recordRepository.startRecord(record().copy(status = WorkoutStatus.PENDING_UPLOAD))

        repository.restore()

        assertNull(repository.state.value.record)
    }

    /**
     * GIVEN: 일시정지한 기록일 때
     * WHEN: 경로 좌표를 더하면
     * THEN: 더하지 않는다
     */
    @Test
    fun `일시정지 중에는 경로 좌표를 더하지 않는다`() = runTest {
        repository.start(record())
        repository.pause()

        repository.addTrackPoint(NextTrackPoint(point(1), addedDistanceMeters = 10.0))

        assertTrue(repository.state.value.trackPoints.isEmpty())
        assertTrue(recordRepository.getTrackPoints(ACTIVITY_ID).getOrThrow().isEmpty())
    }

    /**
     * GIVEN: 기록 중일 때
     * WHEN: 일시정지하면
     * THEN: 기기에도 일시정지 상태로 저장된다
     */
    @Test
    fun `일시정지하면 기기에 저장한다`() = runTest {
        repository.start(record())
        clock.now = START.plusSeconds(60)

        repository.pause()

        val saved = recordRepository.getRecord().getOrThrow()
        assertEquals(WorkoutStatus.ACTIVITY_PAUSED, saved?.status)
        assertEquals(60_000L, saved?.durationMillis)
    }

    private fun record() = WorkoutRecord(
        activityId = ACTIVITY_ID,
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

    private fun point(index: Int) = TrackPoint(
        pointIndex = index,
        latitude = 37.0,
        longitude = 127.0,
        elevation = 100.0,
        recordedAt = START.plusSeconds(index.toLong()),
    )

    private class TestClock(var now: Instant) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC

        override fun withZone(zone: ZoneId?): Clock = this

        override fun instant(): Instant = now
    }

    private companion object {
        const val ACTIVITY_ID = 1L
        val START: Instant = Instant.ofEpochMilli(1_000_000)
    }
}
