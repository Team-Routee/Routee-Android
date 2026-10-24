package com.routee.android.feature.timeline.main

import com.routee.android.core.data.recap.repository.RecapRepository
import com.routee.android.core.data.workout.repository.WorkoutRemoteRepository
import com.routee.android.feature.timeline.main.model.TimelineDraft
import com.routee.android.feature.timeline.main.model.TimelineSaveResult
import javax.inject.Inject
import timber.log.Timber

internal class TimelineRecordSaver @Inject constructor(
    private val workoutRemoteRepository: WorkoutRemoteRepository,
    private val recapRepository: RecapRepository,
) {
    // 종료 때 막 저장한 기록이라 서버 루트는 비어 있음
    private var savedRoutePoints = emptyList<String>()
    private var savedTitle: String? = null

    suspend fun save(activityId: Long, draft: TimelineDraft): TimelineSaveResult {
        if (draft.routePoints != savedRoutePoints) {
            workoutRemoteRepository.updateRoutes(activityId, draft.routePoints)
                .onFailure { Timber.e(it, "루트 저장 실패") }
                .getOrElse { return TimelineSaveResult.Failed }
            savedRoutePoints = draft.routePoints
        }
        val title = draft.title
        if (title != null && title != savedTitle) {
            recapRepository.updateRecapTitle(activityId, title)
                .onFailure { Timber.e(it, "제목 수정 실패") }
                .getOrElse { return TimelineSaveResult.Failed }
            savedTitle = title
        }
        return TimelineSaveResult.Success
    }
}
