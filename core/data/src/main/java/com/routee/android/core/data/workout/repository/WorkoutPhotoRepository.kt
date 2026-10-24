package com.routee.android.core.data.workout.repository

import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutPhoto
import java.io.File
import java.time.Instant
import kotlinx.coroutines.flow.StateFlow

/** 진행 중인 운동의 사진, 기기에서 먼저 바꾸고 [syncPhotos]로 서버에 반영 */
interface WorkoutPhotoRepository {
    /** 지금 기록의 사진, 찍은 순서 */
    val photos: StateFlow<List<WorkoutPhoto>>

    /** 카메라가 원본을 쓸 빈 파일 */
    suspend fun createCaptureFile(): Result<File>

    /**
     * 카메라 원본을 압축해 사진 추가
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

    /** 장소명 수정 */
    fun updateTitle(
        photoId: String,
        title: String,
    )

    /** 사진 삭제, 올린 사진은 다음 [syncPhotos] 때 서버에서도 지움 */
    fun removePhoto(photoId: String)

    /** 못 올린 사진 업로드, 바뀐 장소명 수정, 지운 사진 삭제를 서버에 반영. 끝까지 못 올린 사진 수 반환 */
    suspend fun syncPhotos(activityId: Long): Int

    /** 저장을 마쳤거나 저장할 수 없는 기록의 사진 삭제 */
    suspend fun clear(): Result<Unit>
}
