package com.routee.android.core.domain.workout

import com.routee.android.core.data.workout.model.TrackPoint
import com.routee.android.core.data.workout.model.WorkoutPhoto
import com.routee.android.core.data.workout.repository.WorkoutPhotoRepository
import java.io.File
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class FakeWorkoutPhotoRepository(
    private val missingCount: Int = 0,
) : WorkoutPhotoRepository {
    private val _photos = MutableStateFlow<List<WorkoutPhoto>>(emptyList())
    override val photos: StateFlow<List<WorkoutPhoto>> = _photos
    val uploadedActivityIds = mutableListOf<Long>()
    val discarded = mutableListOf<File>()
    var isCleared = false

    fun setPhotos(photos: List<WorkoutPhoto>) {
        _photos.value = photos
    }

    override suspend fun createCaptureFile(): Result<File> = Result.success(File("capture.jpg"))

    override suspend fun addPhoto(
        activityId: Long,
        original: File,
        point: TrackPoint,
        takenAt: Instant,
    ): Result<WorkoutPhoto> {
        val photo = WorkoutPhoto(
            id = "photo-${_photos.value.size + 1}",
            activityId = activityId,
            filePath = original.path,
            point = point,
            takenAt = takenAt,
        )
        _photos.value += photo
        return Result.success(photo)
    }

    override suspend fun discardCaptureFile(file: File) {
        discarded += file
    }

    override suspend fun uploadPhotos(activityId: Long): Int {
        uploadedActivityIds += activityId
        return missingCount
    }

    override suspend fun clear(): Result<Unit> {
        isCleared = true
        _photos.value = emptyList()
        return Result.success(Unit)
    }
}
