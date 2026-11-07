package com.routee.android.core.domain.timeline

import com.routee.android.core.data.recap.model.ActivityRecap
import com.routee.android.core.data.recap.model.ActivityTrack
import com.routee.android.core.data.recap.model.RecapEditorData
import com.routee.android.core.data.recap.model.RecapResource
import com.routee.android.core.data.recap.model.TimelineMarker
import com.routee.android.core.data.recap.repository.RecapRepository
import com.routee.android.core.domain.workout.FakePhotoTrackingRepository
import com.routee.android.core.domain.workout.FakeWorkoutPhotoRepository
import com.routee.android.core.domain.workout.photoTrackPoint
import com.routee.android.core.domain.workout.workoutPhoto
import java.io.File
import java.time.YearMonth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTimelineTrackUseCaseTest {
    private val tracking = FakePhotoTrackingRepository(points = 0)
    private val photos = FakeWorkoutPhotoRepository()
    private val recap = FakeRecapRepository()
    private val getTimelineTrack = GetTimelineTrackUseCase(tracking, photos, recap)

    /**
     * GIVEN: 저장을 마친 기록의 경로가 기기에 남아 있을 때
     * WHEN: 경로를 조회하면
     * THEN: 서버를 부르지 않고 기기 경로와 이 기록의 올린 사진만 마커로 돌려준다
     */
    @Test
    fun `기기에 남긴 경로가 있으면 서버를 부르지 않는다`() = runTest {
        tracking.savedRoute = ACTIVITY_ID to listOf(photoTrackPoint(0), photoTrackPoint(1))
        photos.setPhotos(
            listOf(
                workoutPhoto(1).copy(timelineId = 11),
                workoutPhoto(2),
                workoutPhoto(3, activityId = 8).copy(timelineId = 13),
            ),
        )

        val result = getTimelineTrack(ACTIVITY_ID).getOrThrow()

        assertEquals(listOf(0, 1), result.trackPoints.map { it.pointIndex })
        assertEquals(
            listOf(TimelineMarker(11, File("photo-1.jpg").toURI().toString(), 37.0, 127.0, 1)),
            result.timelineMarkers,
        )
        assertEquals(0, recap.trackRequestCount)
    }

    /**
     * GIVEN: 앱이 다시 켜져 기기에 경로가 없을 때
     * WHEN: 경로를 조회하면
     * THEN: 서버에서 조회한다
     */
    @Test
    fun `기기에 경로가 없으면 서버에서 조회한다`() = runTest {
        val result = getTimelineTrack(ACTIVITY_ID).getOrThrow()

        assertEquals(SERVER_TRACK, result)
        assertEquals(1, recap.trackRequestCount)
    }

    private class FakeRecapRepository : RecapRepository {
        var trackRequestCount = 0

        override suspend fun getMonthlyRecaps(month: YearMonth): Result<List<ActivityRecap>> = error("Unused")

        override suspend fun updateRecapTitle(
            activityId: Long,
            title: String,
        ): Result<Unit> = error("Unused")

        override suspend fun getRecapResource(activityId: Long): Result<RecapResource> = error("Unused")

        override suspend fun getActivityTrack(activityId: Long): Result<ActivityTrack> {
            trackRequestCount++
            return Result.success(SERVER_TRACK)
        }

        override suspend fun getRecapEditorData(activityId: Long): Result<RecapEditorData> = error("Unused")
    }

    private companion object {
        const val ACTIVITY_ID = 7L
        val SERVER_TRACK = ActivityTrack(emptyList(), emptyList())
    }
}
