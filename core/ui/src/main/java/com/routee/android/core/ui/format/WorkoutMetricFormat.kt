package com.routee.android.core.ui.format

import java.util.Locale

private const val METERS_PER_KILOMETER = 1000.0
private const val SECONDS_PER_HOUR = 3600
private const val SECONDS_PER_MINUTE = 60

/**
 * 거리 km 표기. 소수 둘째 자리 (예: 15530 → "15.53")
 */
fun formatDistanceKm(distanceMeters: Int): String =
    String.format(Locale.ROOT, "%.2f", distanceMeters / METERS_PER_KILOMETER)

/**
 * 운동 시간 시:분 표기 (예: 12000 → "03:20")
 */
fun formatDurationClock(durationSec: Int): String =
    String.format(
        Locale.ROOT,
        "%02d:%02d",
        durationSec / SECONDS_PER_HOUR,
        durationSec % SECONDS_PER_HOUR / SECONDS_PER_MINUTE,
    )

/**
 * 운동 시간 h · m 표기 (예: 12000 → "03h 20m")
 */
fun formatDurationHourMinute(durationSec: Int): String =
    String.format(
        Locale.ROOT,
        "%02dh %02dm",
        durationSec / SECONDS_PER_HOUR,
        durationSec % SECONDS_PER_HOUR / SECONDS_PER_MINUTE,
    )

/**
 * 운동 시간 h m 표기, 앞자리 0 없음 (예: 4800 → "1h 20m")
 */
fun formatDurationHourMinuteShort(durationSec: Int): String =
    "${durationSec / SECONDS_PER_HOUR}h ${durationSec % SECONDS_PER_HOUR / SECONDS_PER_MINUTE}m"
