package com.routee.android.core.data.timeline.repository

import com.routee.android.core.data.timeline.model.TimelinePhotoUpload
import com.routee.android.core.data.timeline.model.TimelinePhotoUploadResult

/** 사진별로 업로드하고 타임라인을 등록한다. 성공한 clientId는 같은 프로세스에서 재등록하지 않는다 */
interface TimelinePhotoRepository {
    /** 촬영 사진 목록의 업로드와 사진별 등록 결과 반환 */
    suspend fun uploadPhotos(activityId: Long, photos: List<TimelinePhotoUpload>): List<TimelinePhotoUploadResult>
}
