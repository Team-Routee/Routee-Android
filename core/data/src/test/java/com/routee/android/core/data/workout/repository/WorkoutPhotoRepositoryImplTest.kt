package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.archive.model.ArchiveDetail
import com.routee.android.core.data.archive.model.ArchivePhoto
import com.routee.android.core.data.archive.repository.ArchiveDetailRepository
import com.routee.android.core.data.recap.model.ActivityRecap
import com.routee.android.core.data.recap.model.ActivityTrack
import com.routee.android.core.data.recap.model.RecapEditorData
import com.routee.android.core.data.recap.model.RecapResource
import com.routee.android.core.data.recap.model.TimelineMarker
import com.routee.android.core.data.recap.repository.RecapRepository
import com.routee.android.core.data.timeline.model.TimelinePhotoUpload
import com.routee.android.core.data.timeline.model.TimelinePhotoUploadResult
import com.routee.android.core.data.timeline.repository.TimelinePhotoRepository
import com.routee.android.core.data.workout.local.WorkoutPhotoLocalDataSource
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.time.Instant
import java.time.YearMonth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutPhotoRepositoryImplTest {
    private val recap = FakeRecapRepository()
    private val archive = FakeArchiveDetailRepository()
    private val directory: File = Files.createTempDirectory("workout_photo").toFile()
    private val repository = WorkoutPhotoRepositoryImpl(
        localDataSource = WorkoutPhotoLocalDataSource { directory },
        timelinePhotoRepository = FakeTimelinePhotoRepository(),
        recapRepository = recap,
        archiveDetailRepository = archive,
    )

    /**
     * GIVEN: 앱이 다시 켜져 사진 목록이 비었고 서버에 올린 사진이 있을 때
     * WHEN: 복구하면
     * THEN: 마커 좌표에 타임라인 목록의 장소명, 큰 이미지를 합쳐 올린 사진으로 채운다
     */
    @Test
    fun `서버 마커와 타임라인 목록으로 사진을 채운다`() = runTest {
        repository.restore(ACTIVITY_ID).getOrThrow()

        val photo = repository.photos.value.single()
        assertEquals(ACTIVITY_ID, photo.activityId)
        assertNull(photo.filePath)
        assertEquals("https://image/large.jpg", photo.serverImageUrl)
        assertEquals("말바위", photo.title)
        assertEquals("말바위", photo.serverTitle)
        assertEquals(9L, photo.timelineId)
        assertEquals(3, photo.point.pointIndex)
        assertEquals(37.5, photo.point.latitude, 0.0)
    }

    /**
     * GIVEN: 타임라인 목록만 받지 못할 때
     * WHEN: 복구하면
     * THEN: 마커 썸네일과 빈 장소명으로 채운다
     */
    @Test
    fun `타임라인 목록을 못 받으면 썸네일로 채운다`() = runTest {
        archive.photosError = IOException()

        repository.restore(ACTIVITY_ID).getOrThrow()

        val photo = repository.photos.value.single()
        assertEquals("https://image/small.jpg", photo.serverImageUrl)
        assertEquals("", photo.title)
    }

    /**
     * GIVEN: 이미 지금 기록의 사진이 있을 때
     * WHEN: 복구하면
     * THEN: 서버를 부르지 않고 그대로 둔다
     */
    @Test
    fun `이미 사진이 있으면 그대로 둔다`() = runTest {
        repository.restore(ACTIVITY_ID).getOrThrow()
        recap.trackCallCount = 0

        repository.restore(ACTIVITY_ID).getOrThrow()

        assertEquals(0, recap.trackCallCount)
        assertEquals(1, repository.photos.value.size)
    }

    /**
     * GIVEN: 경로 조회가 실패할 때
     * WHEN: 복구하면
     * THEN: 실패를 돌려주고 목록은 비워 둔다
     */
    @Test
    fun `경로 조회가 실패하면 실패한다`() = runTest {
        recap.trackError = IOException()

        assertTrue(repository.restore(ACTIVITY_ID).isFailure)
        assertTrue(repository.photos.value.isEmpty())
    }

    private class FakeRecapRepository : RecapRepository {
        var trackError: Throwable? = null
        var trackCallCount = 0

        override suspend fun getActivityTrack(activityId: Long): Result<ActivityTrack> {
            trackCallCount++
            trackError?.let { return Result.failure(it) }
            return Result.success(
                ActivityTrack(
                    trackPoints = emptyList(),
                    timelineMarkers = listOf(
                        TimelineMarker(
                            timelineId = 9,
                            thumbnailUrl = "https://image/small.jpg",
                            latitude = 37.5,
                            longitude = 127.0,
                            pointIndex = 3,
                        ),
                    ),
                ),
            )
        }

        override suspend fun getMonthlyRecaps(month: YearMonth): Result<List<ActivityRecap>> =
            Result.failure(UnsupportedOperationException())

        override suspend fun updateRecapTitle(
            activityId: Long,
            title: String,
        ): Result<Unit> = Result.failure(UnsupportedOperationException())

        override suspend fun getRecapResource(activityId: Long): Result<RecapResource> =
            Result.failure(UnsupportedOperationException())

        override suspend fun getRecapEditorData(activityId: Long): Result<RecapEditorData> =
            Result.failure(UnsupportedOperationException())
    }

    private class FakeArchiveDetailRepository : ArchiveDetailRepository {
        var photosError: Throwable? = null

        override suspend fun getPhotos(activityId: Long): Result<List<ArchivePhoto>> {
            photosError?.let { return Result.failure(it) }
            return Result.success(
                listOf(
                    ArchivePhoto(
                        id = 9,
                        location = "말바위",
                        imageUrl = "https://image/large.jpg",
                        createdAt = Instant.parse("2026-10-24T01:00:00Z"),
                    ),
                ),
            )
        }

        override suspend fun getDetail(activityId: Long): Result<ArchiveDetail> =
            Result.failure(UnsupportedOperationException())

        override suspend fun updateTitle(
            activityId: Long,
            title: String,
        ): Result<Unit> = Result.failure(UnsupportedOperationException())

        override suspend fun deleteActivity(activityId: Long): Result<Unit> =
            Result.failure(UnsupportedOperationException())
    }

    private class FakeTimelinePhotoRepository : TimelinePhotoRepository {
        override suspend fun uploadPhotos(
            activityId: Long,
            photos: List<TimelinePhotoUpload>,
        ): List<TimelinePhotoUploadResult> = emptyList()

        override suspend fun updateTitle(
            activityId: Long,
            timelineId: Long,
            title: String,
        ): Result<Unit> = Result.success(Unit)

        override suspend fun deletePhoto(
            activityId: Long,
            timelineId: Long,
        ): Result<Unit> = Result.success(Unit)

        override suspend fun clearUploadState(activityId: Long) = Unit
    }

    private companion object {
        const val ACTIVITY_ID = 1L
    }
}
