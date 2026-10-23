package com.routee.android.core.domain.workout

import com.routee.android.core.data.timeline.repository.TimelineMapImageRepository
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.repository.WorkoutRecordRepository
import com.routee.android.core.data.workout.repository.WorkoutRemoteRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import java.io.File
import javax.inject.Inject

/** 종료한 운동 저장, 지도 캡처를 올리고 완료 API 호출. 실패하면 기기 기록은 업로드 대기로 남음 */
class SaveFinishedWorkoutUseCase @Inject constructor(
    private val trackingRepository: WorkoutTrackingRepository,
    private val recordRepository: WorkoutRecordRepository,
    private val mapImageRepository: TimelineMapImageRepository,
    private val remoteRepository: WorkoutRemoteRepository,
) {
    /**
     * @param mapImage 경로 전체를 담은 지도 캡처 JPEG, 캡처 실패면 null
     */
    suspend operator fun invoke(mapImage: ByteArray?): WorkoutSaveResult {
        val state = trackingRepository.state.value
        val record = state.finishedRecord ?: return WorkoutSaveResult.Failed
        if (!state.isSavable) {
            trackingRepository.clear()
            return WorkoutSaveResult.InvalidRoute
        }
        val objectKey = mapImage?.let { uploadMapImage(record, it) } ?: return WorkoutSaveResult.Failed
        remoteRepository.completeWorkout(record, state.trackPoints, objectKey)
            .getOrElse { return WorkoutSaveResult.Failed }
        trackingRepository.clear()
        return WorkoutSaveResult.Saved
    }

    // 캡처를 기기에 저장해 올리고, 재전송 때 다시 올리지 않게 키를 기기 기록에 남김
    private suspend fun uploadMapImage(
        record: WorkoutRecord,
        jpeg: ByteArray,
    ): String? {
        val saved = recordRepository.saveMapImage(record, jpeg).getOrNull() ?: return null
        val imagePath = saved.mapImagePath ?: return null
        val objectKey = mapImageRepository.uploadMapImage(record.activityId, File(imagePath)).getOrNull()
            ?: return null
        recordRepository.saveRecord(saved.copy(mapImageObjectKey = objectKey))
        return objectKey
    }
}

/** 종료한 운동 저장 결과 */
enum class WorkoutSaveResult {
    Saved,

    /** 경로 좌표가 2개 미만이거나 운동 시간이 1초 미만이라 저장할 수 없음, 기기 기록은 지움 */
    InvalidRoute,

    /** 업로드나 완료 API 실패, 기기 기록은 업로드 대기로 남음 */
    Failed,
}
