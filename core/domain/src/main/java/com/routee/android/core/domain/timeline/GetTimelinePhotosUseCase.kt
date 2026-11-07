package com.routee.android.core.domain.timeline

import com.routee.android.core.data.archive.model.ArchivePhoto
import com.routee.android.core.data.archive.repository.ArchiveDetailRepository
import com.routee.android.core.data.workout.model.WorkoutPhoto
import com.routee.android.core.data.workout.repository.WorkoutPhotoRepository
import java.io.File
import javax.inject.Inject

/** 타임라인 사진 목록, 기기에 올린 사진이 있으면 서버를 부르지 않음 */
class GetTimelinePhotosUseCase @Inject constructor(
    private val photoRepository: WorkoutPhotoRepository,
    private val archiveDetailRepository: ArchiveDetailRepository,
) {
    suspend operator fun invoke(activityId: Long): Result<List<ArchivePhoto>> {
        val devicePhotos = photoRepository.photos.value.toTimelinePhotos(activityId)
        if (devicePhotos.isNotEmpty()) return Result.success(devicePhotos)
        return archiveDetailRepository.getPhotos(activityId)
    }
}

// 서버에 올린 사진만, 찍은 순서
private fun List<WorkoutPhoto>.toTimelinePhotos(activityId: Long): List<ArchivePhoto> =
    filter { it.activityId == activityId }
        .mapNotNull { photo ->
            val timelineId = photo.timelineId ?: return@mapNotNull null
            val imageUrl = photo.imageUrl() ?: return@mapNotNull null
            ArchivePhoto(timelineId, photo.title.takeIf(String::isNotBlank), imageUrl, photo.takenAt)
        }
        .sortedBy { it.createdAt }

// 기기 파일이 있으면 file: 주소, 없으면 서버 주소
internal fun WorkoutPhoto.imageUrl(): String? = filePath?.let { File(it).toURI().toString() } ?: serverImageUrl
