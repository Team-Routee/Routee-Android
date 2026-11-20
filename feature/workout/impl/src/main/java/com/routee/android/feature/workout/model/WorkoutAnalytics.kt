package com.routee.android.feature.workout.model

import com.routee.android.core.analytics.AnalyticsEvent
import com.routee.android.core.analytics.AnalyticsProperty
import com.routee.android.core.analytics.AnalyticsTracker
import com.routee.android.core.data.workout.model.UnfinishedWorkout
import com.routee.android.core.domain.workout.WorkoutSaveResult

internal enum class WorkoutRecoveryReason {
    APP_RESTARTED,
    PAUSE_EXPIRED,
    SAVE_FAILED,
}

internal val UnfinishedWorkout.Recoverable.recoveryReason: WorkoutRecoveryReason
    get() = if (isAppRestarted) WorkoutRecoveryReason.APP_RESTARTED else WorkoutRecoveryReason.PAUSE_EXPIRED

// 경로가 짧아 저장할 수 없는 기록은 보내지 않음
internal fun AnalyticsTracker.trackWorkoutSaveResult(
    activityId: Long,
    result: WorkoutSaveResult,
) {
    val event = when (result) {
        WorkoutSaveResult.Saved, WorkoutSaveResult.PhotosMissing -> AnalyticsEvent.WORKOUT_COMPLETED

        WorkoutSaveResult.InvalidRoute -> return

        WorkoutSaveResult.Rejected, WorkoutSaveResult.MapImageMissing, WorkoutSaveResult.Failed ->
            AnalyticsEvent.WORKOUT_COMPLETE_FAILED
    }
    track(event, AnalyticsProperty.workout(activityId))
}
