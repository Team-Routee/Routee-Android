package com.routee.android.feature.timeline.main

import com.routee.android.core.data.recap.model.ActivityRecap
import com.routee.android.core.data.recap.model.ActivityTrack
import com.routee.android.core.data.recap.model.RecapEditorData
import com.routee.android.core.data.recap.model.RecapResource
import com.routee.android.core.data.recap.repository.RecapRepository
import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.repository.WorkoutRemoteRepository
import com.routee.android.core.network.exception.ApiException
import com.routee.android.feature.timeline.main.model.TimelineDraft
import com.routee.android.feature.timeline.main.model.TimelineRequestResult
import java.io.IOException
import java.time.Instant
import java.time.YearMonth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineRecordSaverTest {
    private val workoutRemote = FakeWorkoutRemoteRepository()
    private val recap = FakeRecapRepository()
    private val saver = TimelineRecordSaver(workoutRemote, recap)

    /**
     * GIVEN: 나의 루트가 비어 있고 제목을 바꾸지 않았을 때
     * WHEN: 저장하면
     * THEN: 아무 요청도 보내지 않고 성공
     */
    @Test
    fun `바뀐 게 없으면 요청하지 않는다`() = runTest {
        val result = saver.save(ACTIVITY_ID, TimelineDraft(title = null, routePoints = emptyList()))

        assertEquals(TimelineRequestResult.Success, result)
        assertEquals(emptyList<List<String>>(), workoutRemote.savedRoutes)
        assertEquals(emptyList<String>(), recap.savedTitles)
    }

    /**
     * GIVEN: 루트와 제목을 한 번 저장했을 때
     * WHEN: 같은 내용으로 다시 저장하면
     * THEN: 다시 보내지 않는다
     */
    @Test
    fun `저장한 내용과 같으면 다시 보내지 않는다`() = runTest {
        val draft = TimelineDraft(title = "북한산", routePoints = listOf("창의문", "청운대"))

        saver.save(ACTIVITY_ID, draft)
        saver.save(ACTIVITY_ID, draft)

        assertEquals(listOf(listOf("창의문", "청운대")), workoutRemote.savedRoutes)
        assertEquals(listOf("북한산"), recap.savedTitles)
    }

    /**
     * GIVEN: 루트를 저장한 뒤
     * WHEN: 지점을 모두 지우고 다시 저장하면
     * THEN: 빈 목록을 보내 서버 루트를 비운다
     */
    @Test
    fun `저장한 루트를 모두 지우면 빈 목록을 보낸다`() = runTest {
        saver.save(ACTIVITY_ID, TimelineDraft(title = null, routePoints = listOf("창의문")))

        val result = saver.save(ACTIVITY_ID, TimelineDraft(title = null, routePoints = emptyList()))

        assertEquals(TimelineRequestResult.Success, result)
        assertEquals(listOf(listOf("창의문"), emptyList()), workoutRemote.savedRoutes)
    }

    /**
     * GIVEN: 루트는 저장되고 제목 저장이 실패했을 때
     * WHEN: 다시 저장하면
     * THEN: 루트는 다시 보내지 않고 제목만 보낸다
     */
    @Test
    fun `제목만 실패했으면 다시 저장할 때 제목만 보낸다`() = runTest {
        val draft = TimelineDraft(title = "북한산", routePoints = listOf("창의문"))
        recap.failure = IOException()

        val failed = saver.save(ACTIVITY_ID, draft)
        recap.failure = null
        val retried = saver.save(ACTIVITY_ID, draft)

        assertEquals(TimelineRequestResult.Failed, failed)
        assertEquals(TimelineRequestResult.Success, retried)
        assertEquals(listOf(listOf("창의문")), workoutRemote.savedRoutes)
        assertEquals(listOf("북한산"), recap.savedTitles)
    }

    /**
     * GIVEN: 루트 저장이 실패할 때
     * WHEN: 저장하면
     * THEN: 실패를 돌려주고 제목은 보내지 않는다
     */
    @Test
    fun `루트 저장이 실패하면 제목을 보내지 않는다`() = runTest {
        workoutRemote.failure = IOException()

        val result = saver.save(ACTIVITY_ID, TimelineDraft(title = "북한산", routePoints = listOf("창의문")))

        assertEquals(TimelineRequestResult.Failed, result)
        assertEquals(emptyList<String>(), recap.savedTitles)
    }

    /**
     * GIVEN: 인터넷 연결이 끊겨 루트 저장이 실패할 때
     * WHEN: 저장하면
     * THEN: 네트워크 실패를 돌려준다
     */
    @Test
    fun `인터넷 연결 실패는 네트워크 실패로 돌려준다`() = runTest {
        workoutRemote.failure = ApiException.NetworkConnection(IOException())

        val result = saver.save(ACTIVITY_ID, TimelineDraft(title = null, routePoints = listOf("창의문")))

        assertEquals(TimelineRequestResult.NetworkFailed, result)
    }

    private class FakeWorkoutRemoteRepository : WorkoutRemoteRepository {
        val savedRoutes = mutableListOf<List<String>>()
        var failure: Throwable? = null

        override suspend fun updateRoutes(
            activityId: Long,
            names: List<String>,
        ): Result<Unit> {
            failure?.let { return Result.failure(it) }
            savedRoutes += names
            return Result.success(Unit)
        }

        override suspend fun createWorkout(
            type: WorkoutType,
            startedAt: Instant,
        ): Result<StartedWorkout> = error("Unused")

        override suspend fun updateStatus(
            activityId: Long,
            status: WorkoutStatus,
        ): Result<Unit> = error("Unused")

        override suspend fun completeWorkout(
            record: WorkoutRecord,
            trackPoints: List<TrackPoint>,
            mapImageObjectKey: String,
        ): Result<Unit> = error("Unused")
    }

    private class FakeRecapRepository : RecapRepository {
        val savedTitles = mutableListOf<String>()
        var failure: Throwable? = null

        override suspend fun updateRecapTitle(
            activityId: Long,
            title: String,
        ): Result<Unit> {
            failure?.let { return Result.failure(it) }
            savedTitles += title
            return Result.success(Unit)
        }

        override suspend fun getMonthlyRecaps(month: YearMonth): Result<List<ActivityRecap>> = error("Unused")

        override suspend fun getRecapResource(activityId: Long): Result<RecapResource> = error("Unused")

        override suspend fun getActivityTrack(activityId: Long): Result<ActivityTrack> = error("Unused")

        override suspend fun getRecapEditorData(activityId: Long): Result<RecapEditorData> = error("Unused")
    }

    private companion object {
        const val ACTIVITY_ID = 7L
    }
}
