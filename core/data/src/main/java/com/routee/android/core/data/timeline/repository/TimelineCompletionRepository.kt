package com.routee.android.core.data.timeline.repository

import com.routee.android.core.data.timeline.model.TimelineCompletion

/** 지도 이미지 키를 받은 뒤 운동 기록과 루트 지점을 저장하는 저장소. */
interface TimelineCompletionRepository {
    /** 운동 완료 후 이름이 있는 루트 지점 저장 */
    suspend fun complete(input: TimelineCompletion, mapImageObjectKey: String): Result<Unit>
}
