package com.routee.android.core.data.timeline.repository

import android.graphics.Bitmap

/** 지도 스냅샷을 업로드해 기록 완료에 사용할 object key를 얻는 저장소. */
interface TimelineMapImageRepository {
    suspend fun uploadMapImage(activityId: Long, bitmap: Bitmap): Result<String>
}
