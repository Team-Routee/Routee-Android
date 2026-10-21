package com.routee.android.core.data.recap.repository

import com.routee.android.core.data.recap.model.ActivityRecap
import com.routee.android.core.data.recap.model.ActivityTrack
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
}
