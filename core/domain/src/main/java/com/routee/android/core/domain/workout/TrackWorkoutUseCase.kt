package com.routee.android.core.domain.workout

import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** 운동 위치 기록, 기록 중일 때만 위치를 받아 경로에 쌓고 기록이 없거나 끝나면 끝남. 위치 기록 서비스가 부름 */
class TrackWorkoutUseCase @Inject constructor(
    private val trackingRepository: WorkoutTrackingRepository,
    private val locationRepository: LocationRepository,
) {
    /**
     * @param onTrackPointFailure  경로 좌표 저장 실패 시 호출, 기록은 계속
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(onTrackPointFailure: (Throwable) -> Unit = {}): Result<Unit> = suspendRunCatching {
        trackingRepository.restore().getOrThrow()
        coroutineScope {
            val tracking = launch {
                trackingRepository.state
                    .map { it.isRecording }
                    .distinctUntilChanged()
                    .flatMapLatest { isRecording ->
                        if (isRecording) locationRepository.observeLocations() else emptyFlow()
                    }
                    .collect { location -> trackingRepository.addTrackPoint(location).onFailure(onTrackPointFailure) }
            }
            trackingRepository.state.first { it.record?.isTracking != true }
            tracking.cancel()
        }
    }
}
