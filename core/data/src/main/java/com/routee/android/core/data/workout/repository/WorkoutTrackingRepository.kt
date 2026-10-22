package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import kotlinx.coroutines.flow.StateFlow

/** 진행 중인 운동 기록, 위치 기록 서비스와 화면이 같이 씀 */
interface WorkoutTrackingRepository {
    val state: StateFlow<WorkoutTrackingState>

    /** 시작 API로 받은 기록으로 새 기록 시작 */
    suspend fun start(record: WorkoutRecord): Result<Unit>

    suspend fun pause(): Result<Unit>

    suspend fun resume(): Result<Unit>

    /** [location]으로 경로 좌표를 만들어 저장, 기록 중이 아니거나 쓸 수 없는 위치면 그냥 성공 */
    suspend fun addTrackPoint(location: DeviceLocation): Result<Unit>

    /** 진행 중인 기록이 없으면 기기에 저장된 기록을 이어감, 꺼져 있던 시간은 뺌 */
    suspend fun restore(): Result<Unit>
}
