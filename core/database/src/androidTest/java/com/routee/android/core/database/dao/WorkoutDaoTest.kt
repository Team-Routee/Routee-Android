package com.routee.android.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.routee.android.core.database.RouteeDatabase
import com.routee.android.core.database.entity.ActivityEntity
import com.routee.android.core.database.entity.TrackPointEntity
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
     * WHEN: 상태를 바꿔 upsertActivity 하면
     * THEN: 기록은 바뀌고 경로 점은 남는다
     */
    @Test
    fun upsertActivityKeepsTrackPoints() = runTest {
        dao.upsertActivity(activity())
        dao.insertTrackPoint(point(0))

        dao.upsertActivity(activity().copy(activityStatus = "PENDING_UPLOAD"))

        assertEquals("PENDING_UPLOAD", dao.getActivity()?.activityStatus)
        assertEquals(1, dao.getTrackPoints(ACTIVITY_ID).size)
    }

    /**
     * GIVEN: 기록이 있을 때
     * WHEN: 경로 점과 갱신된 기록을 함께 저장하면
     * THEN: 경로 점과 거리가 모두 저장된다
     */
    @Test
    fun insertTrackPointSavesPointAndActivity() = runTest {
        dao.upsertActivity(activity())

        dao.insertTrackPoint(activity().copy(distance = 12.5), point(0))

        assertEquals(listOf(point(0)), dao.getTrackPoints(ACTIVITY_ID))
        assertEquals(12.5, dao.getActivity()?.distance)
    }

    /**
     * GIVEN: 이전 기록과 경로 점이 있을 때
     * WHEN: 새 기록으로 replaceActivity 하면
     * THEN: 새 기록만 남고 이전 경로 점은 지워진다
     */
    @Test
    fun replaceActivityRemovesPreviousActivityAndPoints() = runTest {
        dao.upsertActivity(activity(id = 1))
        dao.insertTrackPoint(point(0, activityId = 1))

        dao.replaceActivity(activity(id = 2))

        assertEquals(2L, dao.getActivity()?.id)
        assertTrue(dao.getTrackPoints(1).isEmpty())
    }

    /**
     * GIVEN: 경로 점이 있는 기록일 때
     * WHEN: 기록을 지우면
     * THEN: 경로 점도 같이 지워진다
     */
    @Test
    fun deleteActivityCascadesTrackPoints() = runTest {
        dao.upsertActivity(activity())
        dao.insertTrackPoint(point(0))

        dao.deleteActivity(ACTIVITY_ID)

        assertNull(dao.getActivity())
        assertTrue(dao.getTrackPoints(ACTIVITY_ID).isEmpty())
    }

    private fun activity(id: Long = ACTIVITY_ID) = ActivityEntity(
        id = id,
        title = "2026.10.22 기록",
        activityType = "HIKING",
        activityStatus = "ACTIVITY_IN_PROGRESS",
        startedAt = 1_000,
        durationMillis = 0,
        segmentStartedAt = 1_000,
        pausedAt = null,
        lastRecordedAt = 1_000,
        distance = 0.0,
        maxElevation = null,
        endedAt = null,
        mapImageObjectKey = null,
        mapImagePath = null,
    )

    private fun point(
        index: Int,
        activityId: Long = ACTIVITY_ID,
    ) = TrackPointEntity(
        activityId = activityId,
        pointIndex = index,
        latitude = 37.5,
        longitude = 127.0,
        elevation = 120.0,
        recordedAt = 2_000L + index,
    )

    private companion object {
        const val ACTIVITY_ID = 1L
    }
}
