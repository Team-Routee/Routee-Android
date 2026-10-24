package com.routee.android.core.domain.workout

import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.timeline.repository.TimelineMapImageRepository
import com.routee.android.core.data.workout.exception.WorkoutException
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.repository.WorkoutPhotoRepository
import com.routee.android.core.data.workout.repository.WorkoutRecordRepository
import com.routee.android.core.data.workout.repository.WorkoutRemoteRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import java.io.File
import javax.inject.Inject

/** 종료한 운동 저장, 지도 캡처와 못 올린 사진을 올리고 완료 API 호출. 앱이 다시 켜졌으면 기기에 남은 업로드 대기 기록을 보냄 */
class SaveFinishedWorkoutUseCase @Inject constructor(
    private val trackingRepository: WorkoutTrackingRepository,
    private val recordRepository: WorkoutRecordRepository,
    private val mapImageRepository: TimelineMapImageRepository,
    private val remoteRepository: WorkoutRemoteRepository,
    private val photoRepository: WorkoutPhotoRepository,
) {
    /**
     * @param mapImage 경로 전체를 담은 지도 캡처 JPEG, null 이면 기기에 저장해 둔 지도 키나 캡처 파일을 씀
     */
    suspend operator fun invoke(mapImage: ByteArray?): WorkoutSaveResult {
        trackingRepository.loadPendingUpload().onFailure { return WorkoutSaveResult.Failed }
        val state = trackingRepository.state.value
        val record = state.finishedRecord ?: return WorkoutSaveResult.Failed
        if (!state.isSavable) {
            clearRecord()
            return WorkoutSaveResult.InvalidRoute
        }
        val objectKey = getMapImageObjectKey(record, mapImage).getOrElse { return failed(it) }
            ?: return WorkoutSaveResult.MapImageMissing
        // 대표 사진은 완료 시점에 정해져서 완료 전에 다시 올리고 장소명, 삭제도 반영. 그래도 못 올린 사진은 빼고 저장
        val missingPhotoCount = photoRepository.syncPhotos(record.activityId)
        remoteRepository.completeWorkout(record, state.trackPoints, objectKey)
            .getOrElse { return failed(it) }
        clearRecord()
        return if (missingPhotoCount > 0) WorkoutSaveResult.PhotosMissing else WorkoutSaveResult.Saved
    }

    private suspend fun clearRecord() {
        trackingRepository.clear()
        photoRepository.clear()
    }

    // 서버가 받지 않는 기록이면 지우고, 인터넷이나 서버 오류면 다시 보낼 수 있게 남김
    private suspend fun failed(error: Throwable): WorkoutSaveResult {
        if (error !is WorkoutException.Rejected) return WorkoutSaveResult.Failed
        clearRecord()
        return WorkoutSaveResult.Rejected
    }

    // 올린 키가 있으면 그대로, 없으면 캡처를 기기에 저장해 올리고 다시 올리지 않게 키를 남김. 캡처가 없으면 null
    private suspend fun getMapImageObjectKey(
        record: WorkoutRecord,
        jpeg: ByteArray?,
    ): Result<String?> = suspendRunCatching {
        record.mapImageObjectKey?.let { return@suspendRunCatching it }
        val saved = if (jpeg != null) recordRepository.saveMapImage(record, jpeg).getOrThrow() else record
        val imagePath = saved.mapImagePath ?: return@suspendRunCatching null
        val objectKey = mapImageRepository.uploadMapImage(record.activityId, File(imagePath)).getOrThrow()
        recordRepository.saveRecord(saved.copy(mapImageObjectKey = objectKey))
        objectKey
    }
}

/** 종료한 운동 저장 결과 */
enum class WorkoutSaveResult {
    Saved,

    /** 저장했지만 끝내 못 올린 사진은 빠짐 */
    PhotosMissing,

    /** 경로 좌표가 2개 미만이거나 운동 시간이 1초 미만이라 저장할 수 없음, 기기 기록은 지움 */
    InvalidRoute,

    /** 서버에 기록이 없거나 보낸 값이 잘못돼 다시 보내도 안 됨, 기기 기록은 지움 */
    Rejected,

    /** 지도 캡처가 없음, 기기 기록은 업로드 대기로 남음. 경로를 다시 캡처해 넘기면 저장 */
    MapImageMissing,

    /** 인터넷, 서버 오류로 업로드나 완료 API 실패, 기기 기록은 업로드 대기로 남음 */
    Failed,
}
