package com.routee.android.feature.workout.tracking

import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.feature.workout.R
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutNotificationContentTest {
    /**
     * GIVEN: 3시간 20분째 기록 중인 기록일 때
     * WHEN: 알림 내용으로 바꾸면
     * THEN: 기록 중 제목, 디자인 표기(15.53, 03:20, 2132), 일시 정지 버튼이 된다
     */
    @Test
    fun `기록 중이면 기록 중 제목과 일시 정지 버튼`() {
        val content = record(
            status = WorkoutStatus.ACTIVITY_IN_PROGRESS,
        ).toNotificationContent(START.plusSeconds(12_000))

        assertEquals(R.string.workout_tracking_title_recording, content.titleRes)
        assertEquals("15.53", content.distance)
        assertEquals("03:20", content.duration)
        assertEquals("2132", content.maxElevation)
        assertEquals(R.string.workout_tracking_action_pause, content.actionLabelRes)
        assertEquals(WorkoutTrackingService.ACTION_PAUSE, content.action)
    }

    /**
     * GIVEN: 일시정지한 기록일 때
     * WHEN: 알림 내용으로 바꾸면
     * THEN: 일시정지 제목과 다시 시작 버튼이 되고 시간은 멈춘 값 그대로다
     */
    @Test
    fun `일시정지면 멈춤 제목과 다시 시작 버튼`() {
        val paused = record(
            status = WorkoutStatus.ACTIVITY_PAUSED,
        ).copy(durationMillis = 12_000_000, resumedAt = null)

        val content = paused.toNotificationContent(START.plusSeconds(99_999))

        assertEquals(R.string.workout_tracking_title_paused, content.titleRes)
        assertEquals("03:20", content.duration)
        assertEquals(R.string.workout_tracking_action_resume, content.actionLabelRes)
        assertEquals(WorkoutTrackingService.ACTION_RESUME, content.action)
    }

    /**
     * GIVEN: 아직 고도를 받지 못한 기록일 때
     * WHEN: 알림 내용으로 바꾸면
     * THEN: 최고 고도는 0으로 보인다
     */
    @Test
    fun `고도가 없으면 최고 고도는 0`() {
        val content = record(
            status = WorkoutStatus.ACTIVITY_IN_PROGRESS,
        ).copy(maxElevationMeters = null).toNotificationContent(START)

        assertEquals("0", content.maxElevation)
    }

    private fun record(status: WorkoutStatus) = WorkoutRecord(
        activityId = 1,
        title = "2026.10.22 기록",
        type = WorkoutType.HIKING,
        status = status,
        startedAt = START,
        durationMillis = 0,
        resumedAt = START,
        pausedAt = null,
        lastRecordedAt = START,
        distanceMeters = 15_530.0,
        maxElevationMeters = 2_132.4,
        endedAt = null,
        mapImageObjectKey = null,
        mapImagePath = null,
    )

    private companion object {
        val START: Instant = Instant.ofEpochMilli(1_000_000)
    }
}
