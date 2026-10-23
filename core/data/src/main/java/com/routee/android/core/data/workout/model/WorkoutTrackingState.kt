package com.routee.android.core.data.workout.model

import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.location.model.distanceMeters
import java.time.Duration
import java.time.Instant

private const val MAX_ACCURACY_METERS = 30f
private val MAX_LOCATION_AGE: Duration = Duration.ofSeconds(10)
private const val MIN_SAVABLE_TRACK_POINTS = 2
private const val MIN_SAVABLE_DURATION_SECONDS = 1

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

    /** 종료해서 업로드를 기다리는 기록, 종료 시각이 없으면 null */
    val finishedRecord: WorkoutRecord?
        get() = record?.takeIf { it.status == WorkoutStatus.PENDING_UPLOAD && it.endedAt != null }

    /** 서버에 저장할 수 있는 종료 기록, 경로 좌표 2개 이상이고 운동 시간 1초 이상 */
    val isSavable: Boolean
        get() {
            val record = finishedRecord ?: return false
            val endedAt = record.endedAt ?: return false
            return trackPoints.size >= MIN_SAVABLE_TRACK_POINTS &&
                record.durationSecondsAt(endedAt) >= MIN_SAVABLE_DURATION_SECONDS
        }

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
        return NextTrackPoint(point, addedDistance, location.usableAltitude())
    }

    /** 경로 좌표를 더한 상태, 기록 중일 때만 */
    fun withTrackPoint(next: NextTrackPoint): WorkoutTrackingState {
        val record = record ?: return this
        if (record.status != WorkoutStatus.ACTIVITY_IN_PROGRESS) return this
        return WorkoutTrackingState(
            record = record.withTrackPoint(next.point, next.addedDistanceMeters, next.elevationMeters),
            trackPoints = trackPoints + next.point,
        )
    }
}

/**
 * 다음 경로 좌표
 *
 * @param addedDistanceMeters  직전 좌표와의 거리, 첫 좌표나 다시 시작한 뒤 첫 좌표는 0
 * @param elevationMeters      최고 고도에 쓸 고도, 고도 오차가 크거나 모르면 null
 */
data class NextTrackPoint(
    val point: TrackPoint,
    val addedDistanceMeters: Double,
    val elevationMeters: Double?,
)

/** 수평 오차 30m 이하, 앞뒤 10초 이내 */
private fun DeviceLocation.isUsableAt(now: Instant): Boolean {
    val accuracy = accuracyMeters ?: return false
    return accuracy <= MAX_ACCURACY_METERS && Duration.between(time, now).abs() <= MAX_LOCATION_AGE
}

/** 고도 오차 30m 이하일 때 고도 */
private fun DeviceLocation.usableAltitude(): Double? {
    val altitudeAccuracy = altitudeAccuracyMeters ?: return null
    if (altitudeAccuracy <= 0f || altitudeAccuracy > MAX_ACCURACY_METERS) return null
    return altitude
}

/** [other]까지 거리 (m), 하버사인 공식 */
private fun TrackPoint.distanceTo(other: TrackPoint): Double =
    distanceMeters(latitude, longitude, other.latitude, other.longitude)
