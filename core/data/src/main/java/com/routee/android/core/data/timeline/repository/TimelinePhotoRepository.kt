package com.routee.android.core.data.timeline.repository

import com.routee.android.core.data.timeline.model.TimelinePhotoUpload
import com.routee.android.core.data.timeline.model.TimelinePhotoUploadResult

/** 사진별 업로드와 타임라인 등록 */
interface TimelinePhotoRepository {
    /** 촬영 사진 목록의 업로드와 사진별 등록 결과 반환 */
    suspend fun uploadPhotos(activityId: Long, photos: List<TimelinePhotoUpload>): List<TimelinePhotoUploadResult>

    /** 활동 저장 완료 후 임시 업로드 상태 정리 */
    suspend fun clearUploadState(activityId: Long)
}
