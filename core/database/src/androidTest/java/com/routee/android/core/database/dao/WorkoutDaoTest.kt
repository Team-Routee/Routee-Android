package com.routee.android.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.routee.android.core.database.RouteeDatabase
import com.routee.android.core.database.entity.TrackPointEntity
import com.routee.android.core.database.entity.WorkoutEntity
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutDaoTest {
    private lateinit var database: RouteeDatabase
    private lateinit var dao: WorkoutDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RouteeDatabase::class.java).build()
        dao = database.workoutDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    /**
     * GIVEN: 경로 점이 있는 기록일 때
     * WHEN: 상태를 바꿔 upsertWorkout 하면
     * THEN: 기록은 바뀌고 경로 점은 남는다
     */
    @Test
    fun upsertWorkoutKeepsTrackPoints() = runTest {
        dao.upsertWorkout(workout())
        dao.insertTrackPoint(point(0))

        dao.upsertWorkout(workout().copy(status = "PENDING_UPLOAD"))

        assertEquals("PENDING_UPLOAD", dao.getWorkout()?.status)
        assertEquals(1, dao.getTrackPoints(WORKOUT_ID).size)
    }

    /**
     * GIVEN: 기록이 있을 때
     * WHEN: 경로 점과 갱신된 기록을 함께 저장하면
     * THEN: 경로 점과 거리가 모두 저장된다
     */
    @Test
    fun insertTrackPointSavesPointAndWorkout() = runTest {
        dao.upsertWorkout(workout())

        dao.insertTrackPoint(workout().copy(distance = 12.5), point(0))

        assertEquals(listOf(point(0)), dao.getTrackPoints(WORKOUT_ID))
        assertEquals(12.5, dao.getWorkout()?.distance)
    }

    /**
     * GIVEN: 이전 기록과 경로 점이 있을 때
     * WHEN: 새 기록으로 replaceWorkout 하면
     * THEN: 새 기록만 남고 이전 경로 점은 지워진다
     */
    @Test
    fun replaceWorkoutRemovesPreviousWorkoutAndPoints() = runTest {
        dao.upsertWorkout(workout(id = 1))
        dao.insertTrackPoint(point(0, workoutId = 1))

        dao.replaceWorkout(workout(id = 2))

        assertEquals(2L, dao.getWorkout()?.id)
        assertTrue(dao.getTrackPoints(1).isEmpty())
    }

    /**
     * GIVEN: 경로 점이 있는 기록일 때
     * WHEN: 기록을 지우면
     * THEN: 경로 점도 같이 지워진다
     */
    @Test
    fun deleteWorkoutCascadesTrackPoints() = runTest {
        dao.upsertWorkout(workout())
        dao.insertTrackPoint(point(0))

        dao.deleteWorkout(WORKOUT_ID)

        assertNull(dao.getWorkout())
        assertTrue(dao.getTrackPoints(WORKOUT_ID).isEmpty())
    }

    private fun workout(id: Long = WORKOUT_ID) = WorkoutEntity(
        id = id,
        title = "2026.10.22 기록",
        type = "HIKING",
        status = "ACTIVITY_IN_PROGRESS",
        startedAt = Instant.ofEpochMilli(1_000),
        durationMillis = 0,
        segmentStartedAt = Instant.ofEpochMilli(1_000),
        pausedAt = null,
        lastRecordedAt = Instant.ofEpochMilli(1_000),
        distance = 0.0,
        maxElevation = null,
        endedAt = null,
        mapImageObjectKey = null,
        mapImagePath = null,
    )

    private fun point(
        index: Int,
        workoutId: Long = WORKOUT_ID,
    ) = TrackPointEntity(
        workoutId = workoutId,
        pointIndex = index,
        latitude = 37.5,
        longitude = 127.0,
        elevation = 120.0,
        recordedAt = Instant.ofEpochMilli(2_000L + index),
    )

    private companion object {
        const val WORKOUT_ID = 1L
    }
}
