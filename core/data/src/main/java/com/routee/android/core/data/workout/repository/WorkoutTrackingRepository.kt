package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.data.workout.model.WorkoutType
import java.time.Instant
import kotlinx.coroutines.flow.StateFlow

/** 진행 중인 운동 기록, 위치 기록 서비스와 화면이 같이 씀 */
interface WorkoutTrackingRepository {
    val state: StateFlow<WorkoutTrackingState>

    /**
     * 새 기록 시작, 운동 시간은 지금부터 셈
     *
     * @param workout   시작 API로 받은 기록
     * @param startedAt 시작 버튼을 누른 시각
     */
    suspend fun start(
        workout: StartedWorkout,
        type: WorkoutType,
        startedAt: Instant,
    ): Result<Unit>

    /** 일시정지, 서버 상태도 바꿈 (서버 실패는 무시) */
    suspend fun pause(): Result<Unit>

    /** 다시 시작, 서버 상태도 바꿈 (서버 실패는 무시) */
    suspend fun resume(): Result<Unit>

    /** [location]으로 경로 좌표를 만들어 저장, 기록 중이 아니거나 쓸 수 없는 위치면 그냥 성공 */
    suspend fun addTrackPoint(location: DeviceLocation): Result<Unit>

    /** 진행 중인 기록이 없으면 기기에 저장된 기록을 이어감, 꺼져 있던 시간은 뺌 */
    suspend fun restore(): Result<Unit>
}
