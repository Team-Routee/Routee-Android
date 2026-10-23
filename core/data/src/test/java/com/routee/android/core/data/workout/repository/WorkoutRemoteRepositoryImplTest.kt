package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutType
import com.routee.android.core.data.workout.remote.dto.request.ActivityTrackPointRequest
import com.routee.android.core.data.workout.remote.dto.request.CompleteActivityRequest
import com.routee.android.core.data.workout.remote.dto.request.CreateActivityRequest
import com.routee.android.core.data.workout.remote.dto.request.UpdateActivityStatusRequest
import com.routee.android.core.data.workout.remote.dto.response.CreateActivityResponse
import com.routee.android.core.data.workout.remote.service.WorkoutService
import com.routee.android.core.network.model.BaseResponse
import com.routee.android.core.network.util.ApiResponseHandler
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class WorkoutRemoteRepositoryImplTest {
    private val service = FakeWorkoutService()
    private val endedAt = Instant.parse("2026-10-23T05:00:00.789Z")
    private val repository =
        WorkoutRemoteRepositoryImpl(service, ApiResponseHandler(Json { ignoreUnknownKeys = true }))

    /**
     * GIVEN: 시작 시각이 밀리초까지 있을 때
     * WHEN: 기록을 생성하면
     * THEN: 운동 종류 이름과 기기 시간대의 초 단위 시각을 보내고 서버가 준 id와 제목을 돌려준다
     */
    @Test
    fun `기록을 생성하면 종류와 초 단위 시작 시각을 보낸다`() = runTest {
        val startedAt = Instant.parse("2026-10-23T00:30:00.123Z")

        val started = repository.createWorkout(WorkoutType.HIKING, startedAt).getOrThrow()

        val expectedStartedAt = LocalDateTime.ofInstant(startedAt.minusMillis(123), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        assertEquals(
            CreateActivityRequest(activityType = "HIKING", startedAt = expectedStartedAt),
            service.createRequest,
        )
        assertEquals(1L, started.activityId)
        assertEquals("2026.10.23 기록", started.title)
    }

    /**
     * GIVEN: 기록이 있을 때
     * WHEN: 일시정지 상태로 바꾸면
     * THEN: 상태 이름을 그대로 보낸다
     */
    @Test
    fun `상태를 바꾸면 상태 이름을 보낸다`() = runTest {
        repository.updateStatus(activityId = 7, status = WorkoutStatus.ACTIVITY_PAUSED).getOrThrow()

        assertEquals(7L to UpdateActivityStatusRequest(status = "ACTIVITY_PAUSED"), service.statusRequest)
    }

    /**
     * GIVEN: 종료한 기록과 경로 좌표가 있을 때
     * WHEN: 완료하면
     * THEN: 거리, 고도는 반올림하고 시간은 초, 종료 시각은 기기 시간대의 초 단위로 보낸다
     */
    @Test
    fun `완료하면 기록 값을 서버 단위로 보낸다`() = runTest {
        repository.completeWorkout(finishedRecord(), listOf(point(1, 90.6), point(2, 101.2)), "map.jpg").getOrThrow()

        val expectedEndedAt = LocalDateTime.ofInstant(endedAt.minusMillis(789), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        assertEquals(
            7L to CompleteActivityRequest(
                title = "2026.10.23 기록",
                distance = 1235,
                durationSec = 3600,
                maxElevation = 116,
                mapImageObjectKey = "map.jpg",
                track = listOf(
                    ActivityTrackPointRequest(latitude = 37.0, longitude = 127.0, elevation = 91, pointIndex = 1),
                    ActivityTrackPointRequest(latitude = 37.0, longitude = 127.0, elevation = 101, pointIndex = 2),
                ),
                endedAt = expectedEndedAt,
            ),
            service.completeRequest,
        )
    }

    /**
     * GIVEN: 서버에서 이미 완료된 기록일 때
     * WHEN: 다시 완료하면
     * THEN: 성공으로 본다
     */
    @Test
    fun `이미 완료된 기록이면 성공으로 본다`() = runTest {
        service.completeError = httpException(409, "ACTIVITY_ALREADY_COMPLETED")

        val result = repository.completeWorkout(finishedRecord(), listOf(point(1), point(2)), "map.jpg")

        assertTrue(result.isSuccess)
    }

    /**
     * GIVEN: 서버에 없는 기록일 때
     * WHEN: 완료하면
     * THEN: 실패를 돌려준다
     */
    @Test
    fun `다른 오류는 실패로 돌려준다`() = runTest {
        service.completeError = httpException(404, "ACTIVITY_NOT_FOUND")

        val result = repository.completeWorkout(finishedRecord(), listOf(point(1), point(2)), "map.jpg")

        assertTrue(result.isFailure)
    }

    private fun finishedRecord() = WorkoutRecord(
        activityId = 7,
        title = "2026.10.23 기록",
        type = WorkoutType.HIKING,
        status = WorkoutStatus.PENDING_UPLOAD,
        startedAt = endedAt.minusSeconds(4_000),
        durationMillis = 3_600_500,
        resumedAt = null,
        pausedAt = null,
        lastRecordedAt = endedAt,
        distanceMeters = 1_234.6,
        maxElevationMeters = 116.4,
        endedAt = endedAt,
        mapImageObjectKey = null,
        mapImagePath = null,
    )

    private fun point(
        index: Int,
        elevation: Double = 90.0,
    ) = TrackPoint(
        pointIndex = index,
        latitude = 37.0,
        longitude = 127.0,
        elevation = elevation,
        recordedAt = endedAt,
    )

    private fun httpException(
        statusCode: Int,
        code: String,
    ): HttpException {
        val body = """{"status":$statusCode,"code":"$code","message":"error"}"""
            .toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(statusCode, body))
    }

    private class FakeWorkoutService : WorkoutService {
        var createRequest: CreateActivityRequest? = null
        var statusRequest: Pair<Long, UpdateActivityStatusRequest>? = null
        var completeRequest: Pair<Long, CompleteActivityRequest>? = null
        var completeError: Throwable? = null

        override suspend fun createWorkout(request: CreateActivityRequest): BaseResponse<CreateActivityResponse> {
            createRequest = request
            return BaseResponse(
                status = 201,
                code = "ACTIVITY_CREATED",
                message = "ok",
                data = CreateActivityResponse(activityId = 1, title = "2026.10.23 기록"),
            )
        }

        override suspend fun updateWorkoutStatus(
            activityId: Long,
            request: UpdateActivityStatusRequest,
        ) {
            statusRequest = activityId to request
        }

        override suspend fun completeWorkout(
            activityId: Long,
            request: CompleteActivityRequest,
        ) {
            completeRequest = activityId to request
            completeError?.let { throw it }
        }
    }
}
