package com.routee.android.core.data.workout.model

import com.routee.android.core.data.location.model.DeviceLocation
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class WorkoutTrackingStateTest {
    /**
     * GIVEN: 경로 좌표가 없는 기록 중 상태일 때
     * WHEN: 위치 두 개로 차례로 경로 좌표를 만들면
     * THEN: 1번부터 번호가 매겨지고 두 번째 좌표부터 거리가 더해진다
     */
    @Test
    fun `경로 좌표는 1번부터 매기고 두 번째부터 거리를 더한다`() {
        val first = requireNotNull(state().nextTrackPoint(location(latitude = 37.0000, seconds = 1), NOW))
        val second =
            requireNotNull(state().withTrackPoint(first).nextTrackPoint(location(latitude = 37.0010, seconds = 2), NOW))

        assertEquals(1, first.point.pointIndex)
        assertEquals(0.0, first.addedDistanceMeters, 0.0)
        assertEquals(2, second.point.pointIndex)
        assertEquals(111.2, second.addedDistanceMeters, 0.5)
    }

    /**
     * GIVEN: 기록 중 상태일 때
     * WHEN: 오차가 30m를 넘거나 10초 넘게 지난 위치면
     * THEN: 경로 좌표를 만들지 않는다
     */
    @Test
    fun `부정확하거나 오래된 위치는 버린다`() {
        assertNull(state().nextTrackPoint(location(seconds = 1, accuracy = 31f), NOW))
        assertNull(state().nextTrackPoint(location(seconds = -11), NOW))
    }

    /**
     * GIVEN: 기록 중 상태일 때
     * WHEN: 기기 시각보다 10초 넘게 앞선 위치가 오면
     * THEN: 경로 좌표를 만들지 않는다
     */
    @Test
    fun `미래 시각 위치는 버린다`() {
        assertNull(state().nextTrackPoint(location(seconds = 11), NOW))
    }

    /**
     * GIVEN: 기록 중 상태일 때
     * WHEN: 고도 오차가 30m를 넘거나 모르는 위치가 오면
     * THEN: 경로 좌표는 만들지만 최고 고도에는 쓰지 않는다
     */
    @Test
    fun `고도 오차가 크거나 모르면 최고 고도에 쓰지 않는다`() {
        val inaccurate = requireNotNull(state().nextTrackPoint(location(seconds = 1, altitudeAccuracy = 31f), NOW))
        val unknown = requireNotNull(state().nextTrackPoint(location(seconds = 1, altitudeAccuracy = null), NOW))
        val accurate = requireNotNull(state().nextTrackPoint(location(seconds = 1), NOW))

        assertNull(inaccurate.elevationMeters)
        assertNull(unknown.elevationMeters)
        assertEquals(100.0, requireNotNull(accurate.elevationMeters), 0.0)
    }

    /**
     * GIVEN: 5초에 기록한 경로 좌표가 있을 때
     * WHEN: 4초 위치가 오면
     * THEN: 경로 좌표를 만들지 않는다
     */
    @Test
    fun `직전 좌표보다 이른 위치는 버린다`() {
        val withPoint = state().withTrackPoint(requireNotNull(state().nextTrackPoint(location(seconds = 5), NOW)))

        assertNull(withPoint.nextTrackPoint(location(seconds = 4), NOW))
    }

    /**
     * GIVEN: 경로 좌표가 있고 그 뒤에 다시 시작한 기록일 때
     * WHEN: 다음 위치가 오면
     * THEN: 직전 좌표와의 거리는 더하지 않는다
     */
    @Test
    fun `다시 시작한 뒤 첫 좌표는 거리에 넣지 않는다`() {
        val withPoint = state().withTrackPoint(
            requireNotNull(state().nextTrackPoint(location(latitude = 37.0000, seconds = 1), NOW)),
        )
        val resumed = withPoint.copy(record = withPoint.record?.copy(resumedAt = NOW.plusSeconds(3)))

        val next = requireNotNull(resumed.nextTrackPoint(location(latitude = 37.0010, seconds = 4), NOW))

        assertEquals(0.0, next.addedDistanceMeters, 0.0)
    }

    /**
     * GIVEN: 일시정지 상태일 때
     * WHEN: 위치가 오거나 경로 좌표를 더하면
     * THEN: 경로 좌표를 만들지 않고 상태도 그대로다
     */
    @Test
    fun `일시정지 중에는 경로 좌표를 만들지도 더하지도 않는다`() {
        val running = state()
        val next = requireNotNull(running.nextTrackPoint(location(seconds = 1), NOW))
        val paused = running.copy(record = running.record?.paused(NOW))

        assertNull(paused.nextTrackPoint(location(seconds = 2), NOW))
        assertSame(paused, paused.withTrackPoint(next))
    }

    private fun state() = WorkoutTrackingState(
        record = WorkoutRecord(
            activityId = 1,
            title = "2026.10.22 기록",
            type = WorkoutType.HIKING,
            status = WorkoutStatus.ACTIVITY_IN_PROGRESS,
            startedAt = NOW,
            durationMillis = 0,
            resumedAt = NOW,
            pausedAt = null,
            lastRecordedAt = NOW,
            distanceMeters = 0.0,
            maxElevationMeters = null,
            endedAt = null,
            mapImageObjectKey = null,
            mapImagePath = null,
        ),
    )

    private fun location(
        latitude: Double = 37.0,
        seconds: Long,
        accuracy: Float = 5f,
        altitudeAccuracy: Float? = 5f,
    ) = DeviceLocation(
        latitude = latitude,
        longitude = 127.0,
        altitude = 100.0,
        altitudeAccuracyMeters = altitudeAccuracy,
        accuracyMeters = accuracy,
        time = NOW.plusSeconds(seconds),
    )

    private companion object {
        val NOW: Instant = Instant.ofEpochMilli(1_000_000)
    }
}
