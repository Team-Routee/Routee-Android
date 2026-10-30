package com.routee.android.core.domain.workout

import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.workout.model.WorkoutStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetWorkoutPhotoPointUseCaseTest {
    /**
     * GIVEN: 경로 좌표가 쌓인 기록 중일 때
     * WHEN: 사진 좌표를 정하면
     * THEN: 마지막 경로 좌표를 준다
     */
    @Test
    fun `마지막 경로 좌표를 준다`() = runTest {
        val point = getPoint(FakePhotoTrackingRepository(points = 3))

        assertEquals(3, point?.pointIndex)
    }

    /**
     * GIVEN: 기록 중이지만 경로 좌표가 아직 없을 때
     * WHEN: 사진 좌표를 정하면
     * THEN: 마지막 위치로 첫 경로 좌표를 만들어 준다
     */
    @Test
    fun `경로 좌표가 없으면 마지막 위치로 첫 좌표를 만든다`() = runTest {
        val tracking = FakePhotoTrackingRepository(points = 0)

        val point = getPoint(tracking, lastLocation = PHOTO_LOCATION)

        assertEquals(listOf(PHOTO_LOCATION), tracking.added)
        assertEquals(1, point?.pointIndex)
    }

    /**
     * GIVEN: 경로 좌표도 마지막 위치도 없을 때
     * WHEN: 사진 좌표를 정하면
     * THEN: null 을 준다
     */
    @Test
    fun `위치를 알 수 없으면 null`() = runTest {
        assertNull(getPoint(FakePhotoTrackingRepository(points = 0)))
    }

    /**
     * GIVEN: 종료해서 업로드를 기다리는 기록일 때
     * WHEN: 사진 좌표를 정하면
     * THEN: null 을 준다
     */
    @Test
    fun `기록 중이 아니면 null`() = runTest {
        assertNull(getPoint(FakePhotoTrackingRepository(points = 1, status = WorkoutStatus.PENDING_UPLOAD)))
    }

    private suspend fun getPoint(
        tracking: FakePhotoTrackingRepository,
        lastLocation: DeviceLocation? = null,
    ) = GetWorkoutPhotoPointUseCase(
        trackingRepository = tracking,
        locationRepository = FakeLastLocationRepository(lastLocation),
    )()
}
