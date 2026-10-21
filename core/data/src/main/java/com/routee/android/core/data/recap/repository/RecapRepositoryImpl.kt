package com.routee.android.core.data.recap.repository

import com.routee.android.core.data.recap.model.ActivityRecap
import com.routee.android.core.data.recap.remote.dto.request.UpdateRecapTitleRequest
import com.routee.android.core.data.recap.remote.dto.response.ActivityRecapResponse
import com.routee.android.core.data.recap.remote.service.RecapService
import com.routee.android.core.network.util.ApiResponseHandler
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

internal class RecapRepositoryImpl @Inject constructor(
    private val recapService: RecapService,
    private val apiResponseHandler: ApiResponseHandler,
) : RecapRepository {

    override suspend fun getMonthlyRecaps(month: YearMonth): Result<List<ActivityRecap>> =
        apiResponseHandler.safeApiCall {
            recapService.getMonthlyRecaps(year = month.year, month = month.monthValue)
        }.mapCatching { response -> response.activities.map { it.toActivityRecap() } }

    override suspend fun updateRecapTitle(
        activityId: Long,
        title: String,
    ): Result<Unit> = apiResponseHandler.safeUnitApiCall {
        recapService.updateRecapTitle(activityId = activityId, request = UpdateRecapTitleRequest(title = title))
    }

    private fun ActivityRecapResponse.toActivityRecap() = ActivityRecap(
        activityId = activityId,
        title = title,
        activityDate = LocalDate.parse(activityDate),
        timelineImageUrls = timelineImageUrls,
    )
}
