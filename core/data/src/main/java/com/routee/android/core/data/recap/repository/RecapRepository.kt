package com.routee.android.core.data.recap.repository

import com.routee.android.core.data.recap.model.ActivityRecap
import com.routee.android.core.data.recap.model.ActivityTrack
import com.routee.android.core.data.recap.model.RecapEditorData
import com.routee.android.core.data.recap.model.RecapResource
import java.time.YearMonth

interface RecapRepository {
    suspend fun getMonthlyRecaps(month: YearMonth): Result<List<ActivityRecap>>

    suspend fun updateRecapTitle(
        activityId: Long,
        title: String,
    ): Result<Unit>

    suspend fun getRecapResource(activityId: Long): Result<RecapResource>

    suspend fun getActivityTrack(activityId: Long): Result<ActivityTrack>

    /**
     * 기록 편집 화면 데이터. 리캡 리소스와 경로를 동시에 불러오고, 하나라도 실패하면 나머지 요청은 취소한다
     */
    suspend fun getRecapEditorData(activityId: Long): Result<RecapEditorData>
}
