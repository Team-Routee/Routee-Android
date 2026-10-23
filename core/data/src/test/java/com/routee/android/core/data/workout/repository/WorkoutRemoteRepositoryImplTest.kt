package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutType
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
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutRemoteRepositoryImplTest {
    private val service = FakeWorkoutService()
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

    private class FakeWorkoutService : WorkoutService {
        var createRequest: CreateActivityRequest? = null
        var statusRequest: Pair<Long, UpdateActivityStatusRequest>? = null

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
    }
}
