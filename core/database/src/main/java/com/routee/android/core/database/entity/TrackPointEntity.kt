package com.routee.android.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * 운동 경로 점 테이블 (`track_point`), 기록이 지워지면 같이 삭제
 *
 * @param activityId  기록 `id`
 * @param pointIndex  경로 순서, 사진 위치 연결에 사용
 * @param elevation   고도 (m)
 * @param recordedAt  기록 시각 (epoch ms)
 */
@Entity(
    tableName = "track_point",
    primaryKeys = ["activity_id", "point_index"],
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activity_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TrackPointEntity(
    @ColumnInfo(name = "activity_id") val activityId: Long,
    @ColumnInfo(name = "point_index") val pointIndex: Int,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    @ColumnInfo(name = "recorded_at") val recordedAt: Long,
)
