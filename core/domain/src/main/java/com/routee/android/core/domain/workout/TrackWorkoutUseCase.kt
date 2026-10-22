package com.routee.android.core.domain.workout

import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import timber.log.Timber

/** 운동 위치 기록, 취소될 때까지 기록 중일 때만 위치를 받아 경로에 쌓음. 위치 기록 서비스가 부름 */
class TrackWorkoutUseCase @Inject constructor(
    private val trackingRepository: WorkoutTrackingRepository,
    private val locationRepository: LocationRepository,
    private val clock: Clock,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(): Result<Unit> = suspendRunCatching {
        trackingRepository.restore().getOrThrow()
        trackingRepository.state
            .map { it.isRecording }
            .distinctUntilChanged()
            .flatMapLatest { isRecording -> if (isRecording) locationRepository.observeLocations() else emptyFlow() }
            .collect { location ->
                trackingRepository.state.value.nextTrackPoint(location, clock.instant())?.let {
                    trackingRepository.addTrackPoint(it).onFailure { e -> Timber.e(e) }
                }
            }
    }
}
