package com.routee.android.core.data.workout.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
