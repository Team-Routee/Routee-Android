package com.routee.android.core.domain.auth

import com.routee.android.core.data.image.repository.ImageCacheRepository
import com.routee.android.core.domain.workout.DiscardWorkoutUseCase
import javax.inject.Inject

/** 기기에 남은 사용자 데이터 전체 삭제, 로그아웃, 탈퇴, 로그인 만료 때 부름. 기록 중이면 위치 기록 서비스도 멈춤 */
class ClearDeviceDataUseCase @Inject constructor(
    private val discardWorkout: DiscardWorkoutUseCase,
    private val imageCacheRepository: ImageCacheRepository,
) {
    /** 하나가 실패해도 나머지는 지우고, 첫 실패를 돌려줌 */
    suspend operator fun invoke(): Result<Unit> {
        val results = listOf(discardWorkout(), imageCacheRepository.clear())
        return results.firstOrNull { it.isFailure } ?: Result.success(Unit)
    }
}
