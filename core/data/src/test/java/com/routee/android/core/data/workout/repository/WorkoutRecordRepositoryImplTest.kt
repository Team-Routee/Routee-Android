package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.workout.local.WorkoutMapImageLocalDataSource
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutType
import java.io.File
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WorkoutRecordRepositoryImplTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var repository: WorkoutRecordRepositoryImpl

    @Before
    fun setUp() {
        val mapImageLocalDataSource = WorkoutMapImageLocalDataSource { tempFolder.root }
        repository = WorkoutRecordRepositoryImpl(FakeWorkoutDao(), mapImageLocalDataSource)
    }

    /**
     * GIVEN: 기록을 시작했을 때
     * WHEN: 경로 점과 갱신된 기록을 저장하면
     * THEN: 경로 점과 갱신된 기록을 그대로 읽을 수 있다
     */
    @Test
    fun `경로 점과 갱신된 기록을 함께 저장한다`() = runTest {
        repository.startRecord(record())
        val updated = record().copy(distanceMeters = 12.5, lastRecordedAt = Instant.ofEpochMilli(5_000))

        repository.saveTrackPoint(updated, point(0))

        assertEquals(listOf(point(0)), repository.getTrackPoints(ACTIVITY_ID).getOrThrow())
        assertEquals(updated, repository.getRecord().getOrThrow())
    }

    /**
     * GIVEN: 이전 기록에 지도 캡처가 있을 때
     * WHEN: 새 기록을 시작하면
     * THEN: 이전 지도 캡처가 지워지고 새 기록만 남는다
     */
    @Test
    fun `새 기록을 시작하면 이전 지도 캡처를 지운다`() = runTest {
        repository.startRecord(record(activityId = 1))
        val path =
            requireNotNull(repository.saveMapImage(record(activityId = 1), byteArrayOf(1)).getOrThrow().mapImagePath)

        repository.startRecord(record(activityId = 2))

        assertFalse(File(path).exists())
        assertEquals(2L, repository.getRecord().getOrThrow()?.activityId)
    }

    /**
     * GIVEN: 지도 캡처가 있는 기록일 때
     * WHEN: 기록을 지우면
     * THEN: 기록과 지도 캡처가 모두 지워진다
     */
    @Test
    fun `기록을 지우면 지도 캡처도 지운다`() = runTest {
        repository.startRecord(record())
        val path = requireNotNull(repository.saveMapImage(record(), byteArrayOf(1)).getOrThrow().mapImagePath)

        repository.deleteRecord(ACTIVITY_ID)

        assertNull(repository.getRecord().getOrThrow())
        assertFalse(File(path).exists())
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

    /**
     * GIVEN: 지도 캡처 파일을 쓸 수 없을 때
     * WHEN: 지도 캡처를 저장하면
     * THEN: 실패를 돌려주고 기록에는 파일 경로가 저장되지 않는다
     */
    @Test
    fun `지도 캡처 파일 저장에 실패하면 실패를 돌려준다`() = runTest {
        val unwritable = File(tempFolder.newFile("not_directory"), "workout_map")
        val repository = WorkoutRecordRepositoryImpl(FakeWorkoutDao(), WorkoutMapImageLocalDataSource { unwritable })
        repository.startRecord(record())

        val result = repository.saveMapImage(record(), byteArrayOf(1))

        assertTrue(result.isFailure)
        assertNull(repository.getRecord().getOrThrow()?.mapImagePath)
    }

    private fun record(activityId: Long = ACTIVITY_ID) = WorkoutRecord(
        activityId = activityId,
        title = "2026.10.22 기록",
        type = WorkoutType.HIKING,
        status = WorkoutStatus.ACTIVITY_IN_PROGRESS,
        startedAt = Instant.ofEpochMilli(1_000),
        durationMillis = 0,
        segmentStartedAt = Instant.ofEpochMilli(1_000),
        pausedAt = null,
        lastRecordedAt = Instant.ofEpochMilli(1_000),
        distanceMeters = 0.0,
        maxElevationMeters = null,
        endedAt = null,
        mapImageObjectKey = null,
        mapImagePath = null,
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
