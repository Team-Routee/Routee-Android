package com.routee.android.core.domain.workout

import com.routee.android.core.data.workout.repository.WorkoutPhotoRepository
import com.routee.android.core.data.workout.repository.WorkoutRecordRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import javax.inject.Inject

/** 기기의 운동 기록 삭제, 진행 중인 기록, 경로, 지도 캡처, 사진 파일. 기록 중이면 위치 기록 서비스도 멈춤. 서버 기록은 그대로 */
class DiscardWorkoutUseCase @Inject constructor(
    private val trackingRepository: WorkoutTrackingRepository,
    private val recordRepository: WorkoutRecordRepository,
    private val photoRepository: WorkoutPhotoRepository,
) {
    /** 하나가 실패해도 나머지는 지우고, 첫 실패를 돌려줌 */
    suspend operator fun invoke(): Result<Unit> {
        val results = listOf(trackingRepository.clear(), recordRepository.clearRecords(), photoRepository.clear())
        return results.firstOrNull { it.isFailure } ?: Result.success(Unit)
    }
}
