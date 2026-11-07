package com.routee.android.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * 운동 중 찍은 사진 테이블 (`workout_photo`), 기록이 지워지면 같이 삭제
 *
 * @param id              기기에서 만든 식별자
 * @param workoutId       기록 `id`
 * @param filePath        압축한 JPEG 파일 경로, 서버에서 불러온 사진은 null
 * @param pointIndex      찍은 위치에 연결한 경로 좌표 순서
 * @param timelineId      타임라인 등록 결과, 아직 못 올렸으면 null
 * @param serverTitle     서버에 반영된 장소명
 * @param serverImageUrl  서버 사진 주소
 */
@Entity(
    tableName = "workout_photo",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("workout_id")],
)
data class WorkoutPhotoEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "workout_id") val workoutId: Long,
    @ColumnInfo(name = "file_path") val filePath: String?,
    @ColumnInfo(name = "point_index") val pointIndex: Int,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    @ColumnInfo(name = "recorded_at") val recordedAt: Instant,
    @ColumnInfo(name = "taken_at") val takenAt: Instant,
    val title: String,
    @ColumnInfo(name = "timeline_id") val timelineId: Long?,
    @ColumnInfo(name = "server_title") val serverTitle: String?,
    @ColumnInfo(name = "server_image_url") val serverImageUrl: String?,
)
