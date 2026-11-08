package com.routee.android.core.domain.timeline

import com.routee.android.core.data.archive.model.ArchiveDetail
import com.routee.android.core.data.archive.model.ArchivePhoto
import com.routee.android.core.data.archive.repository.ArchiveDetailRepository
import com.routee.android.core.domain.workout.FakeWorkoutPhotoRepository
import com.routee.android.core.domain.workout.PHOTO_START
import com.routee.android.core.domain.workout.workoutPhoto
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTimelinePhotosUseCaseTest {
    private val photos = FakeWorkoutPhotoRepository()
    private val archiveDetail = FakeArchiveDetailRepository()
    private val getTimelinePhotos = GetTimelinePhotosUseCase(photos, archiveDetail)

    /**
     * GIVEN: 기기에 이 기록의 올린 사진, 못 올린 사진, 다른 기록 사진이 있을 때
     * WHEN: 사진 목록을 조회하면
     * THEN: 서버를 부르지 않고 이 기록의 올린 사진만 찍은 순서로 돌려준다
     */
    @Test
    fun `기기에 올린 사진이 있으면 서버를 부르지 않는다`() = runTest {
        photos.setPhotos(
            listOf(
                workoutPhoto(1).copy(timelineId = 11, takenAt = PHOTO_START.plusSeconds(10)),
                workoutPhoto(2).copy(timelineId = 12),
                workoutPhoto(3),
                workoutPhoto(4, activityId = 8).copy(timelineId = 14),
            ),
        )

        val result = getTimelinePhotos(ACTIVITY_ID).getOrThrow()

        assertEquals(listOf(12L, 11L), result.map { it.id })
        assertEquals(0, archiveDetail.photoRequestCount)
    }

    /**
     * GIVEN: 앱이 다시 켜져 기기에 사진이 없을 때
     * WHEN: 사진 목록을 조회하면
     * THEN: 서버에서 조회한다
     */
    @Test
    fun `기기에 사진이 없으면 서버에서 조회한다`() = runTest {
        val result = getTimelinePhotos(ACTIVITY_ID).getOrThrow()

        assertEquals(SERVER_PHOTOS, result)
        assertEquals(1, archiveDetail.photoRequestCount)
    }

    private class FakeArchiveDetailRepository : ArchiveDetailRepository {
        var photoRequestCount = 0

        override suspend fun getDetail(activityId: Long): Result<ArchiveDetail> = error("Unused")

        override suspend fun getPhotos(activityId: Long): Result<List<ArchivePhoto>> {
            photoRequestCount++
            return Result.success(SERVER_PHOTOS)
        }

        override suspend fun updateTitle(
            activityId: Long,
            title: String,
        ): Result<Unit> = error("Unused")

        override suspend fun deleteActivity(activityId: Long): Result<Unit> = error("Unused")
    }

    private companion object {
        const val ACTIVITY_ID = 7L
        val SERVER_PHOTOS = listOf(ArchivePhoto(21, "정상", "https://image", Instant.EPOCH))
    }
}
