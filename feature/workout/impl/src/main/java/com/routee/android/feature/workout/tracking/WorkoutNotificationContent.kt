package com.routee.android.feature.workout.tracking

import androidx.annotation.StringRes
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.ui.format.formatDistanceKm
import com.routee.android.core.ui.format.formatDurationClock
import com.routee.android.feature.workout.R
import java.time.Instant
import kotlin.math.roundToInt

/**
 * 상시 알림에 보여줄 값
 *
 * @param action  버튼을 누르면 서비스로 보낼 동작
 */
internal data class WorkoutNotificationContent(
    @param:StringRes val titleRes: Int,
    val distance: String,
    val duration: String,
    val maxElevation: String,
    @param:StringRes val actionLabelRes: Int,
    val action: String,
)

internal fun WorkoutRecord.toNotificationContent(now: Instant): WorkoutNotificationContent {
    val isPaused = status == WorkoutStatus.ACTIVITY_PAUSED
    return WorkoutNotificationContent(
        titleRes = if (isPaused) R.string.workout_tracking_title_paused else R.string.workout_tracking_title_recording,
        distance = formatDistanceKm(distanceMeters.roundToInt()),
        duration = formatDurationClock(durationSecondsAt(now)),
        maxElevation = (maxElevationMeters?.roundToInt() ?: 0).toString(),
        actionLabelRes = if (isPaused) {
            R.string.workout_tracking_action_resume
        } else {
            R.string.workout_tracking_action_pause
        },
        action = if (isPaused) WorkoutTrackingService.ACTION_RESUME else WorkoutTrackingService.ACTION_PAUSE,
    )
}
