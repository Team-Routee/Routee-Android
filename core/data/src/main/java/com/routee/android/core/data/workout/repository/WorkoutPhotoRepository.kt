package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutPhoto
import java.io.File
import java.time.Instant
import kotlinx.coroutines.flow.StateFlow

/** 진행 중인 운동의 사진, 찍을 때 올리고 못 올린 사진은 종료 때 다시 올림 */
interface WorkoutPhotoRepository {
    /** 지금 기록의 사진, 찍은 순서 */
    val photos: StateFlow<List<WorkoutPhoto>>

    /** 카메라가 원본을 쓸 빈 파일 */
    suspend fun createCaptureFile(): Result<File>

    /**
     * 카메라 원본을 압축해 사진 추가, 업로드는 [uploadPhotos]
     *
     * @param original  카메라 원본, 압축 후 삭제
     * @param point     찍은 위치에 연결할 경로 좌표
     */
    suspend fun addPhoto(
        activityId: Long,
        original: File,
        point: TrackPoint,
        takenAt: Instant,
    ): Result<WorkoutPhoto>

    /** 카메라를 취소했거나 사진을 쓰지 않을 때 원본 삭제 */
    suspend fun discardCaptureFile(file: File)

    /** 아직 못 올린 사진 업로드와 타임라인 등록, 끝까지 못 올린 사진 수 반환 */
    suspend fun uploadPhotos(activityId: Long): Int

    /** 저장을 마쳤거나 저장할 수 없는 기록의 사진 삭제 */
    suspend fun clear(): Result<Unit>
}
