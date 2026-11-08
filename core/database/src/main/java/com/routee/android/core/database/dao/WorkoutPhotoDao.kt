package com.routee.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.routee.android.core.database.entity.WorkoutPhotoEntity

/** 운동 사진(`workout_photo`) 테이블 읽기, 쓰기 */
@Dao
interface WorkoutPhotoDao {
    /** 기록의 사진, 찍은 순서 */
    @Query("SELECT * FROM workout_photo WHERE workout_id = :workoutId ORDER BY taken_at")
    suspend fun getPhotos(workoutId: Long): List<WorkoutPhotoEntity>

    /** 사진 추가 */
    @Insert
    suspend fun insertPhotos(photos: List<WorkoutPhotoEntity>)

    /** 기록의 사진 삭제 */
    @Query("DELETE FROM workout_photo WHERE workout_id = :workoutId")
    suspend fun deletePhotos(workoutId: Long)

    /** 기록의 사진을 모두 지우고 새 목록 저장 */
    @Transaction
    suspend fun replacePhotos(
        workoutId: Long,
        photos: List<WorkoutPhotoEntity>,
    ) {
        deletePhotos(workoutId)
        insertPhotos(photos)
    }

    /** 모든 사진 삭제 */
    @Query("DELETE FROM workout_photo")
    suspend fun clearPhotos()
}
