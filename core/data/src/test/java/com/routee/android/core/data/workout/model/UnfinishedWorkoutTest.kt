package com.routee.android.core.data.workout.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class UnfinishedWorkoutTest {
    /**
     * GIVEN: 앱이 살아 있고 기록 중일 때
     * WHEN: 앱에 들어오면
     * THEN: 안내 없이 기록 화면으로 간다
     */
    @Test
    fun `앱이 살아 있으면 기록 화면으로 간다`() {
        val result = UnfinishedWorkout.of(current = record(), saved = null, now = START)

        assertEquals(UnfinishedWorkout.Tracking, result)
    }

    /**
     * GIVEN: 앱이 꺼졌다 켜져서 기기에만 기록이 남았을 때
     * WHEN: 앱에 들어오면
     * THEN: 복구를 안내한다
     */
    @Test
    fun `앱이 꺼졌다 켜지면 복구를 안내한다`() {
        val saved = record()

        val result = UnfinishedWorkout.of(current = null, saved = saved, now = START)

        assertEquals(UnfinishedWorkout.Recoverable(saved), result)
    }

    /**
     * GIVEN: 앱이 살아 있고 일시정지한 기록일 때
     * WHEN: 2시간 이내, 2시간이 된 뒤 앱에 들어오면
     * THEN: 2시간 이내는 기록 화면, 넘으면 복구를 안내한다
     */
    @Test
    fun `일시정지 2시간이 넘으면 복구를 안내한다`() {
        val paused = record().paused(START)

        assertEquals(
            UnfinishedWorkout.Tracking,
            UnfinishedWorkout.of(current = paused, saved = null, now = START.plus(WORKOUT_PAUSE_LIMIT).minusSeconds(1)),
        )
        assertEquals(
            UnfinishedWorkout.Recoverable(paused),
            UnfinishedWorkout.of(current = paused, saved = null, now = START.plus(WORKOUT_PAUSE_LIMIT)),
        )
    }

    /**
     * GIVEN: 종료했지만 서버 저장을 확인하지 못한 기록일 때
     * WHEN: 앱에 들어오면
     * THEN: 업로드 대기로 본다
     */
    @Test
    fun `종료한 기록은 업로드 대기로 본다`() {
        val finished = record().finished(START)

        val result = UnfinishedWorkout.of(current = null, saved = finished, now = START)

        assertEquals(UnfinishedWorkout.PendingUpload(finished), result)
    }

    /**
     * GIVEN: 남은 기록이 없을 때
     * WHEN: 앱에 들어오면
     * THEN: 없다고 본다
     */
    @Test
    fun `남은 기록이 없으면 없다고 본다`() {
        assertEquals(UnfinishedWorkout.None, UnfinishedWorkout.of(current = null, saved = null, now = START))
    }

    private fun record() = WorkoutRecord(
        activityId = 1,
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

    private companion object {
        val START: Instant = Instant.ofEpochMilli(1_000_000)
    }
}
