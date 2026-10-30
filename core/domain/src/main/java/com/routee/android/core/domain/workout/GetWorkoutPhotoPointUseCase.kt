package com.routee.android.core.domain.workout

import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import javax.inject.Inject

/** 찍은 사진을 연결할 경로 좌표, 카메라가 돌아왔을 때 정함 */
class GetWorkoutPhotoPointUseCase @Inject constructor(
    private val trackingRepository: WorkoutTrackingRepository,
    private val locationRepository: LocationRepository,
) {
    /** 마지막 경로 좌표, 아직 없으면 마지막 위치로 첫 좌표를 만듦. 기록 중이 아니거나 위치를 모르면 null */
    suspend operator fun invoke(): TrackPoint? {
        val state = trackingRepository.state.value
        if (state.record?.isTracking != true) return null
        state.trackPoints.lastOrNull()?.let { return it }
        val location = locationRepository.getLastLocation().getOrNull() ?: return null
        trackingRepository.addTrackPoint(location)
        return trackingRepository.state.value.trackPoints.lastOrNull()
    }
}
