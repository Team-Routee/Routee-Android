package com.routee.android.core.domain.workout

import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.repository.WorkoutPhotoRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import java.io.File
import java.time.Clock
import javax.inject.Inject

/** 운동 기록 한 번에 찍을 수 있는 사진 수 */
const val MAX_WORKOUT_PHOTO_COUNT = 20

/** 운동 중 찍은 사진 추가, 마지막 경로 좌표에 연결하고 바로 업로드. 업로드 실패는 종료 때 다시 올림 */
class AddWorkoutPhotoUseCase @Inject constructor(
    private val trackingRepository: WorkoutTrackingRepository,
    private val photoRepository: WorkoutPhotoRepository,
    private val locationRepository: LocationRepository,
    private val clock: Clock,
) {
    /**
     * @param original 카메라 원본, 추가하지 못해도 삭제
     */
    suspend operator fun invoke(original: File): AddWorkoutPhotoResult {
        val record = trackingRepository.state.value.record?.takeIf { it.isTracking }
        val count = photoRepository.photos.value.count { it.activityId == record?.activityId }
        if (record == null || count >= MAX_WORKOUT_PHOTO_COUNT) {
            photoRepository.discardCaptureFile(original)
            return AddWorkoutPhotoResult.Failed
        }
        val point = lastTrackPoint() ?: run {
            photoRepository.discardCaptureFile(original)
            return AddWorkoutPhotoResult.NoLocation
        }
        photoRepository.addPhoto(record.activityId, original, point, clock.instant())
            .getOrElse { return AddWorkoutPhotoResult.Failed }
        photoRepository.syncPhotos(record.activityId)
        return AddWorkoutPhotoResult.Added
    }

    // 경로 좌표가 아직 없으면 마지막 위치로 첫 좌표를 만듦
    private suspend fun lastTrackPoint(): TrackPoint? {
        trackingRepository.state.value.trackPoints.lastOrNull()?.let { return it }
        val location = locationRepository.getLastLocation().getOrNull() ?: return null
        trackingRepository.addTrackPoint(location)
        return trackingRepository.state.value.trackPoints.lastOrNull()
    }
}

/** 사진 추가 결과 */
enum class AddWorkoutPhotoResult {
    /** 추가함, 업로드 실패는 종료 때 다시 올림 */
    Added,

    /** 연결할 경로 좌표가 없어 버림 */
    NoLocation,

    /** 기록 중이 아니거나 20장을 넘었거나 압축 실패로 버림 */
    Failed,
}
