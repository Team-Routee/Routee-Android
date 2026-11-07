package com.routee.android.feature.timeline.main.model

import com.routee.android.core.data.archive.model.ArchivePhoto
import com.routee.android.core.data.workout.model.WorkoutPhoto
import java.io.File

/** 기기에 남은 [activityId] 사진 중 서버에 올린 사진, 못 올린 사진은 기록에 없어 뺌 */
internal fun List<WorkoutPhoto>.toTimelinePhotos(activityId: Long): List<ArchivePhoto> =
    filter { it.activityId == activityId }
        .mapNotNull { photo ->
            val timelineId = photo.timelineId ?: return@mapNotNull null
            val imageUrl = photo.filePath?.let { File(it).toURI().toString() }
                ?: photo.serverImageUrl
                ?: return@mapNotNull null
            ArchivePhoto(timelineId, photo.title.takeIf(String::isNotBlank), imageUrl, photo.takenAt)
        }
        .sortedBy { it.createdAt }
