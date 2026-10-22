package com.routee.android.core.data.timeline.repository

import com.routee.android.core.data.timeline.model.TimelineCompletion

/** 운동 기록과 루트 지점 저장 */
interface TimelineCompletionRepository {
    /** 운동 완료 후 이름이 있는 루트 지점 저장 */
    suspend fun complete(input: TimelineCompletion, mapImageObjectKey: String): Result<Unit>
}
