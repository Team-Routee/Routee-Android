package com.routee.android.core.domain.workout

import com.routee.android.core.data.workout.model.WorkoutStatus
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AddWorkoutPhotoUseCaseTest {
    private val photos = FakeWorkoutPhotoRepository()
    private val original = File("capture.jpg")

    /**
     * GIVEN: 기록 중일 때
     * WHEN: 찍을 때 정한 좌표와 장소명으로 사진을 추가하면
     * THEN: 그대로 추가하고 바로 올린다
     */
    @Test
    fun `정한 좌표와 장소명으로 추가하고 바로 올린다`() = runTest {
        val result = addPhoto()

        assertEquals(AddWorkoutPhotoResult.Added, result)
        val photo = photos.photos.value.single()
        assertEquals(2, photo.point.pointIndex)
        assertEquals(PHOTO_NOW, photo.takenAt)
        assertEquals("말바위", photo.title)
        assertEquals(listOf(7L), photos.syncedActivityIds)
    }

    /**
     * GIVEN: 이미 20장을 찍었을 때
     * WHEN: 사진을 추가하면
     * THEN: 사진을 버린다
     */
    @Test
    fun `20장이면 사진을 버린다`() = runTest {
        photos.setPhotos(List(MAX_WORKOUT_PHOTO_COUNT) { workoutPhoto(it) })

        val result = addPhoto()

        assertEquals(AddWorkoutPhotoResult.Failed, result)
        assertEquals(MAX_WORKOUT_PHOTO_COUNT, photos.photos.value.size)
        assertEquals(listOf(original), photos.discarded)
    }

    /**
     * GIVEN: 이전 기록 사진이 20장 남아 있을 때
     * WHEN: 새 기록에서 사진을 추가하면
     * THEN: 지금 기록 사진만 세어 추가한다
     */
    @Test
    fun `이전 기록 사진은 세지 않는다`() = runTest {
        photos.setPhotos(List(MAX_WORKOUT_PHOTO_COUNT) { workoutPhoto(it, activityId = 6) })

        val result = addPhoto()

        assertEquals(AddWorkoutPhotoResult.Added, result)
    }

    /**
     * GIVEN: 종료해서 업로드를 기다리는 기록일 때
     * WHEN: 사진을 추가하면
     * THEN: 사진을 버린다
     */
    @Test
    fun `기록 중이 아니면 사진을 버린다`() = runTest {
        val result = addPhoto(WorkoutStatus.PENDING_UPLOAD)

        assertEquals(AddWorkoutPhotoResult.Failed, result)
        assertEquals(listOf(original), photos.discarded)
    }

    private suspend fun addPhoto(status: WorkoutStatus = WorkoutStatus.ACTIVITY_IN_PROGRESS) = AddWorkoutPhotoUseCase(
        trackingRepository = FakePhotoTrackingRepository(points = 1, status = status),
        photoRepository = photos,
    )(original = original, point = photoTrackPoint(2), takenAt = PHOTO_NOW, title = "말바위")
}
