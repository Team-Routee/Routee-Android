package com.routee.android.core.data.workout.model

import com.routee.android.core.data.location.model.DeviceLocation
import java.time.Duration
import java.time.Instant
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val MAX_ACCURACY_METERS = 30f
private val MAX_LOCATION_AGE: Duration = Duration.ofSeconds(10)
private const val EARTH_RADIUS_METERS = 6_371_008.8

/**
 * 진행 중인 운동 기록
 *
 * @param record       기록 중이 아니면 null
 * @param trackPoints  지금까지 기록한 경로 좌표
 */
data class WorkoutTrackingState(
    val record: WorkoutRecord? = null,
    val trackPoints: List<TrackPoint> = emptyList(),
) {
    /** 기록 중 (일시정지, 종료 후가 아님) */
    val isRecording: Boolean
        get() = record?.status == WorkoutStatus.ACTIVITY_IN_PROGRESS

    /** [location]으로 만들 다음 경로 좌표, 기록 중이 아니거나 부정확하고 오래된 위치면 null */
    fun nextTrackPoint(
        location: DeviceLocation,
        now: Instant,
    ): NextTrackPoint? {
        if (!isRecording || record == null || !location.isUsableAt(now)) return null
        val last = trackPoints.lastOrNull()
        if (last != null && !location.time.isAfter(last.recordedAt)) return null

        val point = TrackPoint(
            pointIndex = (last?.pointIndex ?: 0) + 1,
            latitude = location.latitude,
            longitude = location.longitude,
            elevation = location.altitude ?: last?.elevation ?: 0.0,
            recordedAt = location.time,
        )
        val addedDistance = if (last == null ||
            record.resumedAt?.isAfter(last.recordedAt) == true
        ) {
            0.0
        } else {
            last.distanceTo(point)
        }
        return NextTrackPoint(point, addedDistance)
    }

    /** 경로 좌표를 더한 상태, 기록 중일 때만 */
    fun withTrackPoint(next: NextTrackPoint): WorkoutTrackingState {
        val record = record ?: return this
        if (record.status != WorkoutStatus.ACTIVITY_IN_PROGRESS) return this
        return WorkoutTrackingState(
            record = record.withTrackPoint(next.point, next.addedDistanceMeters),
            trackPoints = trackPoints + next.point,
        )
    }
}

/**
 * 다음 경로 좌표
 *
 * @param addedDistanceMeters  직전 좌표와의 거리, 첫 좌표나 다시 시작한 뒤 첫 좌표는 0
 */
data class NextTrackPoint(
    val point: TrackPoint,
    val addedDistanceMeters: Double,
)

private fun DeviceLocation.isUsableAt(now: Instant): Boolean {
    val accuracy = accuracyMeters ?: return false
    return accuracy <= MAX_ACCURACY_METERS && Duration.between(time, now) <= MAX_LOCATION_AGE
}

/** [other]까지 거리 (m), 하버사인 공식 */
private fun TrackPoint.distanceTo(other: TrackPoint): Double {
    val lat1 = Math.toRadians(latitude)
    val lat2 = Math.toRadians(other.latitude)
    val dLat = lat2 - lat1
    val dLng = Math.toRadians(other.longitude - longitude)
    val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
    return 2 * EARTH_RADIUS_METERS * asin(sqrt(a))
}
