package com.routee.android.feature.recordedit

import com.routee.android.core.data.recap.model.ActivityRecap
import com.routee.android.core.data.recap.model.ActivityTrack
import com.routee.android.core.data.recap.model.RecapResource
import com.routee.android.core.data.recap.repository.RecapRepository
import java.time.YearMonth

internal class FakeRecapRepository : RecapRepository {
    var recaps: List<ActivityRecap> = emptyList()
    var error: Throwable? = null
    val requestedMonths = mutableListOf<YearMonth>()
    var titleError: Throwable? = null
    val updatedTitles = mutableListOf<Pair<Long, String>>()
    var resource = RecapResource(
        distanceMeters = 0,
        durationSeconds = 0,
        maxElevationMeters = 0,
        mapImageUrl = null,
        routes = emptyList(),
    )
    var track = ActivityTrack(trackPoints = emptyList(), timelineMarkers = emptyList())
    var editorError: Throwable? = null

    override suspend fun getMonthlyRecaps(month: YearMonth): Result<List<ActivityRecap>> {
        requestedMonths += month
        return error?.let { Result.failure(it) } ?: Result.success(recaps)
    }

    override suspend fun updateRecapTitle(
        activityId: Long,
        title: String,
    ): Result<Unit> {
        updatedTitles += activityId to title
        return titleError?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    override suspend fun getRecapResource(activityId: Long): Result<RecapResource> =
        editorError?.let { Result.failure(it) } ?: Result.success(resource)

    override suspend fun getActivityTrack(activityId: Long): Result<ActivityTrack> =
        editorError?.let { Result.failure(it) } ?: Result.success(track)
}
