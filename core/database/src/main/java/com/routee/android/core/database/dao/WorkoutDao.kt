package com.routee.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.routee.android.core.database.entity.TrackPointEntity
import com.routee.android.core.database.entity.WorkoutEntity

/** 운동 기록(`workout`), 경로 점(`track_point`) 테이블 읽기, 쓰기 */
@Dao
interface WorkoutDao {
    /** 저장된 기록, 없으면 null */
    @Query("SELECT * FROM workout LIMIT 1")
    suspend fun getWorkout(): WorkoutEntity?

    /** 기록 추가 */
    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity)

    /** 같은 id의 기록 수정, 수정한 행 수 반환 */
    @Update
    suspend fun updateWorkout(workout: WorkoutEntity): Int

    /** 경로 점 추가 */
    @Insert
    suspend fun insertTrackPoint(point: TrackPointEntity)

    /** 경로 점 추가와 기록 갱신(거리, 마지막 기록 시각)을 한 트랜잭션으로, 수정한 기록 행 수 반환 */
    @Transaction
    suspend fun insertTrackPoint(
        workout: WorkoutEntity,
        point: TrackPointEntity,
    ): Int {
        insertTrackPoint(point)
        return updateWorkout(workout)
    }

    /** 기존 기록과 경로 점을 모두 지우고 새 기록 저장 */
    @Transaction
    suspend fun replaceWorkout(workout: WorkoutEntity) {
        deleteWorkouts()
        insertWorkout(workout)
    }

    /** 기록의 경로 점, `point_index` 순서 */
    @Query("SELECT * FROM track_point WHERE workout_id = :workoutId ORDER BY point_index")
    suspend fun getTrackPoints(workoutId: Long): List<TrackPointEntity>

    /** 기록 삭제, 경로 점도 같이 삭제 */
    @Query("DELETE FROM workout WHERE id = :workoutId")
    suspend fun deleteWorkout(workoutId: Long)

    /** 모든 기록과 경로 점 삭제 */
    @Query("DELETE FROM workout")
    suspend fun deleteWorkouts()
}
