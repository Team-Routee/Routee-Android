package com.routee.android.core.data.workout.model

import kotlin.math.roundToInt

private const val MIN_ELEVATION_METERS = -500
private const val MAX_ELEVATION_METERS = 9_000

// 서버로 보내는 고도 (m), 서버 허용 범위(-500~9000)를 벗어나면 요청 전체가 400이라 범위 안으로 자름
internal fun Double.toServerElevation(): Int = roundToInt().coerceIn(MIN_ELEVATION_METERS, MAX_ELEVATION_METERS)
