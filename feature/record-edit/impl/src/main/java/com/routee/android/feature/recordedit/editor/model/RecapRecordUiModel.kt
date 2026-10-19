package com.routee.android.feature.recordedit.editor.model

import androidx.compose.runtime.Immutable
import java.util.Locale

/**
 * 기록 스티커에 보여줄 운동 수치 (GET /api/v1/activity/{activityId}/recap)
 */
@Immutable
internal data class RecapRecordUiModel(
    val distance: String,
    val duration: String,
    val maxElevation: String,
) {
    companion object {
        val EMPTY = RecapRecordUiModel(distance = "", duration = "", maxElevation = "")

        fun from(
            distanceMeters: Int,
            durationSeconds: Int,
            maxElevationMeters: Int,
        ) = RecapRecordUiModel(
            distance = formatDistance(distanceMeters),
            duration = formatDuration(durationSeconds),
            maxElevation = formatElevation(maxElevationMeters),
        )

        fun formatDistance(meters: Int): String =
            if (meters == 0) "" else String.format(Locale.ROOT, "%.2fkm", meters / METERS_PER_KILOMETER)

        fun formatDuration(seconds: Int): String {
            if (seconds == 0) return ""
            return "${seconds / SECONDS_PER_HOUR}h ${seconds % SECONDS_PER_HOUR / SECONDS_PER_MINUTE}m"
        }

        fun formatElevation(meters: Int): String = if (meters == 0) "" else "${meters}m"

        private const val METERS_PER_KILOMETER = 1000.0
        private const val SECONDS_PER_HOUR = 3600
        private const val SECONDS_PER_MINUTE = 60
    }
}
