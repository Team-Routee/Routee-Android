package com.routee.android.core.data.timeline.repository

import android.graphics.Bitmap
import java.io.File

/** 기록 완료용 지도 이미지 업로드 */
interface TimelineMapImageRepository {
    /** 지도 스냅샷 업로드와 완료 요청에 사용할 object key 반환 */
    suspend fun uploadMapImage(activityId: Long, bitmap: Bitmap): Result<String>

    /** 저장해 둔 지도 캡처 파일 업로드, object key 반환 */
    suspend fun uploadMapImage(activityId: Long, imageFile: File): Result<String>
}
