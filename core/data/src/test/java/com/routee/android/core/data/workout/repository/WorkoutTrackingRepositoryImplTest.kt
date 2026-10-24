package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.workout.local.WorkoutMapImageLocalDataSource
import com.routee.android.core.data.workout.model.StartedWorkout
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
    private val remoteRepository = FakeWorkoutRemoteRepository()
    private lateinit var recordRepository: WorkoutRecordRepository
    private lateinit var repository: WorkoutTrackingRepositoryImpl

    @Before
    fun setUp() {
        recordRepository =
            WorkoutRecordRepositoryImpl(FakeWorkoutDao(), WorkoutMapImageLocalDataSource { tempFolder.root })
        repository = WorkoutTrackingRepositoryImpl(recordRepository, remoteRepository, clock)
    }

    /**
     * GIVEN: 시작 버튼을 누르고 3초 뒤에
     * WHEN: 기록을 시작하면
     * THEN: 시작 시각은 누른 시각이고 운동 시간은 지금부터 센다
     */
    @Test
    fun `시작 시각과 운동 시간 기준 시각을 따로 둔다`() = runTest {
        clock.now = START.plusSeconds(3)

        repository.start(STARTED_WORKOUT, WorkoutType.HIKING, START)

        val record = repository.state.value.record
        assertEquals(START, record?.startedAt)
        assertEquals(START.plusSeconds(3), record?.resumedAt)
        assertEquals(ACTIVITY_ID, record?.activityId)
    }

    /**
     * GIVEN: 기록 중일 때
     * WHEN: 일시정지하고 다시 시작하면
     * THEN: 서버에 일시정지, 기록 중 상태를 차례로 보낸다
     */
    @Test
    fun `일시정지와 다시 시작은 서버 상태도 바꾼다`() = runTest {
        repository.start(STARTED_WORKOUT, WorkoutType.HIKING, START)

        repository.pause()
        repository.resume()

        assertEquals(
            listOf(ACTIVITY_ID to WorkoutStatus.ACTIVITY_PAUSED, ACTIVITY_ID to WorkoutStatus.ACTIVITY_IN_PROGRESS),
            remoteRepository.updatedStatuses,
        )
    }

    /**
     * GIVEN: 서버 상태 변경이 실패할 때
     * WHEN: 일시정지하면
     * THEN: 기기 기록은 일시정지되고 성공으로 돌려준다
     */
    @Test
    fun `서버 상태 변경이 실패해도 일시정지는 된다`() = runTest {
        val repository = WorkoutTrackingRepositoryImpl(
            recordRepository,
            FakeWorkoutRemoteRepository(updateStatusError = IllegalStateException()),
            clock,
        )
        repository.start(STARTED_WORKOUT, WorkoutType.HIKING, START)

        val result = repository.pause()

        assertTrue(result.isSuccess)
        assertEquals(WorkoutStatus.ACTIVITY_PAUSED, repository.state.value.record?.status)
    }

    /**
     * GIVEN: 이미 일시정지 중일 때
     * WHEN: 다시 일시정지하면
     * THEN: 서버에 보내지 않는다
     */
    @Test
    fun `상태가 그대로면 서버에 보내지 않는다`() = runTest {
        repository.start(STARTED_WORKOUT, WorkoutType.HIKING, START)
        repository.pause()

        repository.pause()

        assertEquals(1, remoteRepository.updatedStatuses.size)
    }

    /**
     * GIVEN: 기기 저장이 실패할 때
     * WHEN: 일시정지하면
     * THEN: 실패를 돌려주고 기록 중 상태를 유지하며 서버에 보내지 않는다
     */
    @Test
    fun `기기 저장이 실패하면 서버에 보내지 않는다`() = runTest {
        val failingRecordRepository = object : WorkoutRecordRepository by recordRepository {
            override suspend fun saveRecord(
                record: WorkoutRecord,
            ): Result<Unit> = Result.failure(IllegalStateException())
        }
        val repository = WorkoutTrackingRepositoryImpl(failingRecordRepository, remoteRepository, clock)
        repository.start(STARTED_WORKOUT, WorkoutType.HIKING, START)

        val result = repository.pause()

        assertTrue(result.isFailure)
        assertEquals(WorkoutStatus.ACTIVITY_IN_PROGRESS, repository.state.value.record?.status)
        assertTrue(remoteRepository.updatedStatuses.isEmpty())
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
     * GIVEN: 기기에 기록 중으로 남은 기록이 있을 때
     * WHEN: 복구로 불러오면
     * THEN: 경로 좌표와 함께 일시정지로 불러오고 기기에도 저장한다
     */
    @Test
    fun `복구하면 남은 기록을 일시정지로 불러온다`() = runTest {
        recordRepository.startRecord(record())
        recordRepository.saveTrackPoint(record(), point(1))
        clock.now = START.plusSeconds(3_600)

        repository.recover()

        assertEquals(WorkoutStatus.ACTIVITY_PAUSED, repository.state.value.record?.status)
        assertEquals(listOf(1), repository.state.value.trackPoints.map { it.pointIndex })
        assertEquals(WorkoutStatus.ACTIVITY_PAUSED, recordRepository.getRecord().getOrThrow()?.status)
    }

    /**
     * GIVEN: 복구한 기록일 때
     * WHEN: 마지막 기록 시각으로 종료하면
     * THEN: 그 시각으로 업로드 대기가 된다
     */
    @Test
    fun `종료 시각을 정해 종료한다`() = runTest {
        recordRepository.startRecord(record())
        clock.now = START.plusSeconds(3_600)
        repository.recover()

        repository.finish(endedAt = START)

        assertEquals(START, repository.state.value.record?.endedAt)
        assertEquals(WorkoutStatus.PENDING_UPLOAD, recordRepository.getRecord().getOrThrow()?.status)
    }

    /**
     * GIVEN: 일시정지한 기록일 때
     * WHEN: 경로 좌표를 더하면
     * THEN: 더하지 않는다
     */
    @Test
    fun `일시정지 중에는 경로 좌표를 더하지 않는다`() = runTest {
        repository.start(STARTED_WORKOUT, WorkoutType.HIKING, START)
        repository.pause()

        repository.addTrackPoint(location(latitude = 37.0, seconds = 1))

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
        repository.start(STARTED_WORKOUT, WorkoutType.HIKING, START)
        clock.now = START.plusSeconds(60)

        repository.pause()

        val saved = recordRepository.getRecord().getOrThrow()
        assertEquals(WorkoutStatus.ACTIVITY_PAUSED, saved?.status)
        assertEquals(60_000L, saved?.durationMillis)
    }

    /**
     * GIVEN: 경로 좌표를 하나 기록한 뒤 일시정지했다가 다시 시작했을 때
     * WHEN: 다시 시작한 뒤 첫 위치가 오면
     * THEN: 좌표는 더하지만 직전 좌표와의 거리는 넣지 않는다
     */
    @Test
    fun `다시 시작한 뒤 첫 좌표는 거리에 넣지 않는다`() = runTest {
        repository.start(STARTED_WORKOUT, WorkoutType.HIKING, START)
        clock.now = START.plusSeconds(1)
        repository.addTrackPoint(location(latitude = 37.0, seconds = 1))
        clock.now = START.plusSeconds(2)
        repository.pause()
        clock.now = START.plusSeconds(3)
        repository.resume()
        clock.now = START.plusSeconds(4)

        repository.addTrackPoint(location(latitude = 37.001, seconds = 4))

        assertEquals(listOf(1, 2), repository.state.value.trackPoints.map { it.pointIndex })
        assertEquals(0.0, requireNotNull(repository.state.value.record).distanceMeters, 0.0)
    }

    /**
     * GIVEN: 기록 중일 때
     * WHEN: 종료하면
     * THEN: 기기에 업로드 대기로 저장되고 더 이상 기록 중이 아니다
     */
    @Test
    fun `종료하면 기기에 업로드 대기로 저장한다`() = runTest {
        repository.start(STARTED_WORKOUT, WorkoutType.HIKING, START)
        clock.now = START.plusSeconds(30)

        repository.finish()

        assertEquals(WorkoutStatus.PENDING_UPLOAD, recordRepository.getRecord().getOrThrow()?.status)
        assertEquals(false, repository.state.value.record?.isTracking)
    }

    /**
     * GIVEN: 종료한 기록이 있을 때
     * WHEN: 지우면
     * THEN: 기기 기록과 상태가 비워진다
     */
    @Test
    fun `기록을 지우면 기기 기록과 상태를 비운다`() = runTest {
        repository.start(STARTED_WORKOUT, WorkoutType.HIKING, START)
        repository.finish()

        repository.clear()

        assertNull(recordRepository.getRecord().getOrThrow())
        assertNull(repository.state.value.record)
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

    private fun location(
        latitude: Double,
        seconds: Long,
    ) = DeviceLocation(
        latitude = latitude,
        longitude = 127.0,
        altitude = 100.0,
        altitudeAccuracyMeters = 5f,
        accuracyMeters = 5f,
        time = START.plusSeconds(seconds),
    )

    private class TestClock(var now: Instant) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC

        override fun withZone(zone: ZoneId?): Clock = this

        override fun instant(): Instant = now
    }

    private companion object {
        const val ACTIVITY_ID = 1L
        val STARTED_WORKOUT = StartedWorkout(ACTIVITY_ID, "2026.10.22 기록")
        val START: Instant = Instant.ofEpochMilli(1_000_000)
    }
}
