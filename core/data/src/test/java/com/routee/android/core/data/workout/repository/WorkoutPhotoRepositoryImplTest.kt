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
import com.routee.android.core.database.dao.WorkoutPhotoDao
import com.routee.android.core.database.entity.WorkoutPhotoEntity
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
    private val dao = FakeWorkoutPhotoDao()
    private val timelinePhotos = FakeTimelinePhotoRepository()
    private val repository = WorkoutPhotoRepositoryImpl(
        localDataSource = WorkoutPhotoLocalDataSource { directory },
        photoDao = dao,
        timelinePhotoRepository = timelinePhotos,
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

    /**
     * GIVEN: 앱이 다시 켜졌고 기기에 저장한 사진이 있을 때
     * WHEN: 복구하면
     * THEN: 서버를 부르지 않고 기기 사진으로 채운다
     */
    @Test
    fun `기기에 저장한 사진이 있으면 서버를 부르지 않는다`() = runTest {
        dao.rows += photoRow(id = "a", timelineId = 3)

        repository.restore(ACTIVITY_ID).getOrThrow()

        assertEquals(0, recap.trackCallCount)
        assertEquals("a.jpg", repository.photos.value.single().filePath)
    }

    /**
     * GIVEN: 앱이 다시 켜졌고 기기에 저장한 사진을 읽지 못할 때
     * WHEN: 복구하고 사진을 반영한 뒤 다시 복구하면
     * THEN: 서버로 채우거나 기기 사진을 덮어쓰지 않고, 다음 복구 때 다시 읽는다
     */
    @Test
    fun `기기 사진을 못 읽으면 덮어쓰지 않고 다음에 다시 읽는다`() = runTest {
        dao.rows += photoRow(id = "a")
        dao.readError = IOException()

        assertTrue(repository.restore(ACTIVITY_ID).isFailure)
        repository.syncPhotos(ACTIVITY_ID)

        assertEquals(0, recap.trackCallCount)
        assertEquals("a", dao.rows.single().id)

        dao.readError = null
        repository.restore(ACTIVITY_ID).getOrThrow()

        assertEquals("a", repository.photos.value.single().id)
    }

    /**
     * GIVEN: 앱이 다시 켜졌고 기기에 못 올린 사진이 있을 때
     * WHEN: 사진을 반영하면
     * THEN: 다시 올리고 타임라인 등록 결과를 기기에 저장한다
     */
    @Test
    fun `앱이 다시 켜져도 못 올린 사진을 다시 올린다`() = runTest {
        dao.rows += photoRow(id = "a")

        val missing = repository.syncPhotos(ACTIVITY_ID)

        assertEquals(0, missing)
        assertEquals(listOf("a"), timelinePhotos.uploadedIds)
        assertEquals(UPLOADED_TIMELINE_ID, dao.rows.single().timelineId)
    }

    /**
     * GIVEN: 앱이 다시 켜졌고 기기에 못 올린 사진이 있을 때
     * WHEN: 다시 올려도 실패하면
     * THEN: 못 올린 사진으로 센다
     */
    @Test
    fun `다시 올려도 실패하면 못 올린 사진으로 센다`() = runTest {
        dao.rows += photoRow(id = "a")
        timelinePhotos.uploadError = IOException()

        assertEquals(1, repository.syncPhotos(ACTIVITY_ID))
    }

    /**
     * GIVEN: 서버에 올린 사진이 있을 때
     * WHEN: 사진을 지우면
     * THEN: 서버와 기기에서 모두 지운다
     */
    @Test
    fun `올린 사진을 지우면 서버와 기기에서 지운다`() = runTest {
        dao.rows += photoRow(id = "a", timelineId = 3)
        repository.restore(ACTIVITY_ID).getOrThrow()

        repository.deletePhoto("a").getOrThrow()

        assertEquals(listOf(3L), timelinePhotos.deletedTimelineIds)
        assertTrue(repository.photos.value.isEmpty())
        assertTrue(dao.rows.isEmpty())
    }

    /**
     * GIVEN: 서버 삭제가 실패할 때
     * WHEN: 올린 사진을 지우면
     * THEN: 실패를 돌려주고 사진은 그대로 둔다
     */
    @Test
    fun `서버 삭제가 실패하면 사진을 그대로 둔다`() = runTest {
        dao.rows += photoRow(id = "a", timelineId = 3)
        timelinePhotos.deleteError = IOException()
        repository.restore(ACTIVITY_ID).getOrThrow()

        assertTrue(repository.deletePhoto("a").isFailure)

        assertEquals("a", repository.photos.value.single().id)
        assertEquals("a", dao.rows.single().id)
    }

    /**
     * GIVEN: 아직 못 올린 사진을 지웠을 때
     * WHEN: 사진을 반영하면
     * THEN: 서버를 부르지 않고, 지운 사진을 다시 불러오거나 올리지 않는다
     */
    @Test
    fun `못 올린 사진을 지우면 다시 불러오지 않는다`() = runTest {
        dao.rows += photoRow(id = "a")
        repository.restore(ACTIVITY_ID).getOrThrow()

        repository.deletePhoto("a").getOrThrow()
        repository.syncPhotos(ACTIVITY_ID)

        assertTrue(timelinePhotos.deletedTimelineIds.isEmpty())
        assertTrue(timelinePhotos.uploadedIds.isEmpty())
        assertTrue(repository.photos.value.isEmpty())
        assertTrue(dao.rows.isEmpty())
    }

    /**
     * GIVEN: 기기에 저장한 사진이 있을 때
     * WHEN: 사진을 모두 지우면
     * THEN: 저장한 사진도 지운다
     */
    @Test
    fun `사진을 지우면 저장한 사진도 지운다`() = runTest {
        dao.rows += photoRow(id = "a")

        repository.clear().getOrThrow()

        assertTrue(dao.rows.isEmpty())
    }

    private fun photoRow(
        id: String,
        timelineId: Long? = null,
    ) = WorkoutPhotoEntity(
        id = id,
        workoutId = ACTIVITY_ID,
        filePath = "$id.jpg",
        pointIndex = 1,
        latitude = 37.5,
        longitude = 127.0,
        elevation = 0.0,
        recordedAt = Instant.parse("2026-10-24T01:00:00Z"),
        takenAt = Instant.parse("2026-10-24T01:00:00Z"),
        title = "",
        timelineId = timelineId,
        serverTitle = null,
        serverImageUrl = null,
    )

    private class FakeWorkoutPhotoDao : WorkoutPhotoDao {
        val rows = mutableListOf<WorkoutPhotoEntity>()
        var readError: Throwable? = null

        override suspend fun getPhotos(workoutId: Long): List<WorkoutPhotoEntity> {
            readError?.let { throw it }
            return rows.filter { it.workoutId == workoutId }
        }

        override suspend fun insertPhotos(photos: List<WorkoutPhotoEntity>) {
            rows += photos
        }

        override suspend fun deletePhotos(workoutId: Long) {
            rows.removeAll { it.workoutId == workoutId }
        }

        override suspend fun clearPhotos() {
            rows.clear()
        }
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
        val uploadedIds = mutableListOf<String>()
        val deletedTimelineIds = mutableListOf<Long>()
        var uploadError: Throwable? = null
        var deleteError: Throwable? = null

        override suspend fun uploadPhotos(
            activityId: Long,
            photos: List<TimelinePhotoUpload>,
        ): List<TimelinePhotoUploadResult> = photos.map { photo ->
            uploadedIds += photo.clientId
            val result = uploadError?.let { Result.failure(it) } ?: Result.success(UPLOADED_TIMELINE_ID)
            TimelinePhotoUploadResult(clientId = photo.clientId, result = result)
        }

        override suspend fun updateTitle(
            activityId: Long,
            timelineId: Long,
            title: String,
        ): Result<Unit> = Result.success(Unit)

        override suspend fun deletePhoto(
            activityId: Long,
            timelineId: Long,
        ): Result<Unit> {
            deleteError?.let { return Result.failure(it) }
            deletedTimelineIds += timelineId
            return Result.success(Unit)
        }

        override suspend fun clearUploadState(activityId: Long) = Unit
    }

    private companion object {
        const val ACTIVITY_ID = 1L
        const val UPLOADED_TIMELINE_ID = 20L
    }
}
