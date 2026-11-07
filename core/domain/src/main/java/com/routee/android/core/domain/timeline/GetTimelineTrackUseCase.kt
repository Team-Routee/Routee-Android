package com.routee.android.core.domain.timeline

import com.routee.android.core.data.recap.model.ActivityTrack
import com.routee.android.core.data.recap.model.TimelineMarker
import com.routee.android.core.data.recap.model.TrackPoint as ActivityTrackPoint
import com.routee.android.core.data.recap.repository.RecapRepository
import com.routee.android.core.data.workout.model.TrackPoint as WorkoutTrackPoint
import com.routee.android.core.data.workout.model.WorkoutPhoto
import com.routee.android.core.data.workout.repository.WorkoutPhotoRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import javax.inject.Inject

/** 타임라인 지도 경로와 사진 마커, 기기에 남긴 경로가 있으면 서버를 부르지 않음 */
class GetTimelineTrackUseCase @Inject constructor(
    private val trackingRepository: WorkoutTrackingRepository,
    private val photoRepository: WorkoutPhotoRepository,
    private val recapRepository: RecapRepository,
) {
    suspend operator fun invoke(activityId: Long): Result<ActivityTrack> {
        val deviceRoute = trackingRepository.getSavedTrackPoints(activityId)
            ?: return recapRepository.getActivityTrack(activityId)
        val markers = photoRepository.photos.value.toTimelineMarkers(activityId)
        return Result.success(ActivityTrack(deviceRoute.map { it.toActivityTrackPoint() }, markers))
    }
}

private fun WorkoutTrackPoint.toActivityTrackPoint() = ActivityTrackPoint(latitude, longitude, elevation, pointIndex)

// 서버에 올린 사진만
private fun List<WorkoutPhoto>.toTimelineMarkers(activityId: Long): List<TimelineMarker> =
    filter { it.activityId == activityId }
        .mapNotNull { photo ->
            val timelineId = photo.timelineId ?: return@mapNotNull null
            val imageUrl = photo.imageUrl() ?: return@mapNotNull null
            TimelineMarker(timelineId, imageUrl, photo.point.latitude, photo.point.longitude, photo.point.pointIndex)
        }
