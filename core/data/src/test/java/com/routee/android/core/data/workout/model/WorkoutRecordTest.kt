package com.routee.android.core.data.workout.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutRecordTest {
    /**
     * GIVEN: 1분째 기록 중일 때
     * WHEN: 일시정지하면
     * THEN: 1분이 누적되고 다시 시작 시각은 비워진다
     */
    @Test
    fun `일시정지하면 그때까지 시간을 누적한다`() {
        val paused = record().paused(START.plusSeconds(60))

        assertEquals(WorkoutStatus.ACTIVITY_PAUSED, paused.status)
        assertEquals(60_000L, paused.durationMillis)
        assertNull(paused.resumedAt)
        assertEquals(START.plusSeconds(60), paused.pausedAt)
    }

    /**
     * GIVEN: 일시정지한 기록일 때
     * WHEN: 다시 시작하면
     * THEN: 다시 시작 시각부터 시간을 새로 센다
     */
    @Test
    fun `다시 시작하면 그 시각부터 센다`() {
        val resumed = record().paused(START.plusSeconds(60)).resumed(START.plusSeconds(600))

        assertEquals(WorkoutStatus.ACTIVITY_IN_PROGRESS, resumed.status)
        assertEquals(120_000L, resumed.durationMillisAt(START.plusSeconds(660)))
    }

    /**
     * GIVEN: 시작하고 1.999초가 지났을 때
     * WHEN: 초 단위 운동 시간을 구하면
     * THEN: 1초 미만은 버린다
     */
    @Test
    fun `초 단위 운동 시간은 1초 미만을 버린다`() {
        assertEquals(1, record().durationSecondsAt(START.plusMillis(1_999)))
    }

    /**
     * GIVEN: 마지막 기록 뒤 앱이 꺼져 있던 기록일 때
     * WHEN: 다시 켜져서 이어가면
     * THEN: 꺼져 있던 시간은 빼고 지금부터 다시 센다
     */
    @Test
    fun `이어가면 꺼져 있던 시간은 뺀다`() {
        val stopped = record().copy(lastRecordedAt = START.plusSeconds(60))

        val restarted = stopped.restarted(START.plusSeconds(3_600))

        assertEquals(60_000L, restarted.durationMillis)
        assertEquals(START.plusSeconds(3_600), restarted.resumedAt)
    }

    /**
     * GIVEN: 최고 고도가 200m인 기록일 때
     * WHEN: 고도 150m 경로 좌표를 더하면
     * THEN: 최고 고도는 200m 그대로고 거리와 마지막 기록 시각은 갱신된다
     */
    @Test
    fun `낮은 고도의 좌표를 더해도 최고 고도는 유지한다`() {
        val point =
            TrackPoint(
                pointIndex = 1,
                latitude = 37.0,
                longitude = 127.0,
                elevation = 150.0,
                recordedAt = START.plusSeconds(5),
            )

        val updated = record().copy(maxElevationMeters = 200.0)
            .withTrackPoint(point, addedDistanceMeters = 12.5, elevationMeters = 150.0)

        assertEquals(200.0, requireNotNull(updated.maxElevationMeters), 0.0)
        assertEquals(12.5, updated.distanceMeters, 0.0)
        assertEquals(START.plusSeconds(5), updated.lastRecordedAt)
    }

    /**
     * GIVEN: 최고 고도가 200m인 기록일 때
     * WHEN: 쓸 수 있는 고도가 없는 좌표를 더하면
     * THEN: 최고 고도는 그대로다
     */
    @Test
    fun `고도를 쓸 수 없는 좌표는 최고 고도에 넣지 않는다`() {
        val point = TrackPoint(
            pointIndex = 1,
            latitude = 37.0,
            longitude = 127.0,
            elevation = 900.0,
            recordedAt = START.plusSeconds(5),
        )

        val updated = record().copy(maxElevationMeters = 200.0)
            .withTrackPoint(point, addedDistanceMeters = 0.0, elevationMeters = null)

        assertEquals(200.0, requireNotNull(updated.maxElevationMeters), 0.0)
    }

    /**
     * GIVEN: 기록 중일 때
     * WHEN: 종료하면
     * THEN: 업로드 대기로 바뀌고 종료 시각까지 운동 시간을 확정한다
     */
    @Test
    fun `기록 중에 종료하면 종료 시각까지 시간을 확정한다`() {
        val finished = record().finished(START.plusSeconds(90))

        assertEquals(WorkoutStatus.PENDING_UPLOAD, finished.status)
        assertEquals(START.plusSeconds(90), finished.endedAt)
        assertEquals(90_000L, finished.durationMillisAt(START.plusSeconds(500)))
    }

    /**
     * GIVEN: 일시정지 중일 때
     * WHEN: 한참 뒤에 종료하면
     * THEN: 정지한 동안은 운동 시간에 넣지 않는다
     */
    @Test
    fun `일시정지 중에 종료하면 정지한 시간은 넣지 않는다`() {
        val finished = record().paused(START.plusSeconds(60)).finished(START.plusSeconds(600))

        assertEquals(WorkoutStatus.PENDING_UPLOAD, finished.status)
        assertEquals(60_000L, finished.durationMillis)
    }

    /**
     * GIVEN: 이미 업로드 대기인 기록일 때
     * WHEN: 다시 종료하면
     * THEN: 그대로 둔다
     */
    @Test
    fun `이미 종료한 기록은 그대로 둔다`() {
        val finished = record().finished(START.plusSeconds(90))

        assertEquals(finished, finished.finished(START.plusSeconds(300)))
    }

    /**
     * GIVEN: 기록 중에 앱이 꺼졌다가 한참 뒤 다시 켜졌을 때
     * WHEN: 복구 안내를 위해 불러오면
     * THEN: 일시정지로 두고 운동 시간은 마지막 기록 시각까지만 센다
     */
    @Test
    fun `복구하면 일시정지로 두고 마지막 기록 시각까지 센다`() {
        val stopped = record().copy(lastRecordedAt = START.plusSeconds(60))

        val recovered = stopped.recovered(START.plusSeconds(3_600))

        assertEquals(WorkoutStatus.ACTIVITY_PAUSED, recovered.status)
        assertEquals(60_000L, recovered.durationMillisAt(START.plusSeconds(7_200)))
        assertEquals(START.plusSeconds(3_600), recovered.pausedAt)
    }

    /**
     * GIVEN: 일시정지한 기록일 때
     * WHEN: 2시간이 되기 직전과 2시간이 된 때
     * THEN: 2시간이 되어야 일시정지가 끝났다고 본다
     */
    @Test
    fun `일시정지 2시간이 되면 끝났다고 본다`() {
        val paused = record().paused(START.plusSeconds(60))

        assertFalse(paused.isPauseExpiredAt(START.plusSeconds(60).plus(WORKOUT_PAUSE_LIMIT).minusMillis(1)))
        assertTrue(paused.isPauseExpiredAt(START.plusSeconds(60).plus(WORKOUT_PAUSE_LIMIT)))
        assertFalse(record().isPauseExpiredAt(START.plus(WORKOUT_PAUSE_LIMIT)))
    }

    /**
     * GIVEN: 복구한 기록일 때
     * WHEN: 마지막 기록 시각으로 종료하면
     * THEN: 종료 시각은 마지막 기록 시각, 운동 시간은 그대로다
     */
    @Test
    fun `종료 시각을 따로 정해 종료할 수 있다`() {
        val recovered = record().copy(lastRecordedAt = START.plusSeconds(60)).recovered(START.plusSeconds(3_600))

        val finished = recovered.finished(START.plusSeconds(3_700), endedAt = recovered.lastRecordedAt)

        assertEquals(START.plusSeconds(60), finished.endedAt)
        assertEquals(60_000L, finished.durationMillis)
    }

    /**
     * GIVEN: 시작 API가 id와 제목을 줬을 때
     * WHEN: 탭 시각과 카운트다운이 끝난 시각으로 기록을 만들면
     * THEN: 기록 중 상태로 시작 시각은 탭 시각, 운동 시간은 카운트다운 끝부터 센다
     */
    @Test
    fun `시작 직후 기록은 카운트다운이 끝난 시각부터 센다`() {
        val tappedAt = Instant.ofEpochMilli(1_000_000)
        val countdownEnd = tappedAt.plusSeconds(3)

        val record = WorkoutRecord.started(
            StartedWorkout(7, "2026.10.23 기록"),
            WorkoutType.HIKING,
            tappedAt,
            countdownEnd,
        )

        assertEquals(7L, record.activityId)
        assertEquals(WorkoutStatus.ACTIVITY_IN_PROGRESS, record.status)
        assertEquals(tappedAt, record.startedAt)
        assertEquals(60_000L, record.durationMillisAt(countdownEnd.plusSeconds(60)))
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

    private companion object {
        val START: Instant = Instant.ofEpochMilli(1_000_000)
    }
}
