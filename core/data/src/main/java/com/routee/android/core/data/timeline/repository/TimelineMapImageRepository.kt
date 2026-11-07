package com.routee.android.core.data.timeline.repository

import com.routee.android.core.data.workout.exception.WorkoutException

/** 기록 완료용 지도 이미지 업로드 */
interface TimelineMapImageRepository {
    /** 지도 캡처 JPEG 업로드, 완료 요청에 쓸 object key 반환. 서버에 기록이 없으면 [WorkoutException.Rejected] */
    suspend fun uploadMapImage(activityId: Long, jpeg: ByteArray): Result<String>
}
