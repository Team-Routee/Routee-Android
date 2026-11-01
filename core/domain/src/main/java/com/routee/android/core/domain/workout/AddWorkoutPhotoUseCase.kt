package com.routee.android.core.domain.workout

import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.repository.WorkoutPhotoRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import java.io.File
import java.time.Instant
import javax.inject.Inject

/** 운동 기록 한 번에 찍을 수 있는 사진 수 */
const val MAX_WORKOUT_PHOTO_COUNT = 20

/** 운동 중 찍은 사진 추가, 장소명과 함께 바로 업로드. 업로드 실패는 종료 때 다시 올림 */
class AddWorkoutPhotoUseCase @Inject constructor(
    private val trackingRepository: WorkoutTrackingRepository,
    private val photoRepository: WorkoutPhotoRepository,
) {
    /**
     * @param original 카메라 원본, 추가하지 못해도 삭제
     * @param point    찍을 때 정한 경로 좌표 ([ResolveWorkoutPhotoPointUseCase])
     * @param title    장소명, 비어 있어도 됨
     */
    suspend operator fun invoke(
        original: File,
        point: TrackPoint,
        takenAt: Instant,
        title: String,
    ): AddWorkoutPhotoResult {
        val record = trackingRepository.state.value.record?.takeIf { it.isTracking }
        val count = photoRepository.photos.value.count { it.activityId == record?.activityId }
        if (record == null || count >= MAX_WORKOUT_PHOTO_COUNT) {
            photoRepository.discardCaptureFile(original)
            return AddWorkoutPhotoResult.Failed
        }
        photoRepository.addPhoto(record.activityId, original, point, takenAt, title)
            .getOrElse { return AddWorkoutPhotoResult.Failed }
        photoRepository.syncPhotos(record.activityId)
        return AddWorkoutPhotoResult.Added
    }
}

/** 사진 추가 결과 */
enum class AddWorkoutPhotoResult {
    /** 추가함, 업로드 실패는 종료 때 다시 올림 */
    Added,

    /** 기록 중이 아니거나 20장을 넘었거나 압축 실패로 버림 */
    Failed,
}
