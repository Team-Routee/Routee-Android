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

    /** 앱이 꺼졌다 켜졌거나 일시정지가 길어 멈춘 기록을 일시정지로 불러옴, 꺼져 있던 시간은 뺌. 이어서 기록은 [resume], 종료 후 저장은 [finish] */
    suspend fun recover(): Result<Unit>

    /**
     * 종료, 기기 기록을 업로드 대기로 바꿈. 이후 위치 기록 서비스가 멈춤
     *
     * @param endedAt 종료 시각, null 이면 지금
     */
    suspend fun finish(endedAt: Instant? = null): Result<Unit>

    /** 기기에 저장된 업로드 대기 기록을 경로와 함께 불러옴, 저장해 둔 지도 키와 캡처 경로도 반영. 기록 중이면 그대로 */
    suspend fun loadPendingUpload(): Result<Unit>

    /** 저장을 마쳤거나 저장할 수 없는 기록 삭제, 상태 비움 */
    suspend fun clear(): Result<Unit>
}
