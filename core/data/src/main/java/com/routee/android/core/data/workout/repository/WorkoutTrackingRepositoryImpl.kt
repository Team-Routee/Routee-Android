package com.routee.android.core.data.workout.repository

import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.workout.model.StartedWorkout
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.data.workout.model.WorkoutType
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
internal class WorkoutTrackingRepositoryImpl @Inject constructor(
    private val recordRepository: WorkoutRecordRepository,
    private val remoteRepository: WorkoutRemoteRepository,
    private val clock: Clock,
) : WorkoutTrackingRepository {
    private val _state = MutableStateFlow(WorkoutTrackingState())
    override val state: StateFlow<WorkoutTrackingState> = _state.asStateFlow()

    private val mutex = Mutex()

    override suspend fun start(
        workout: StartedWorkout,
        type: WorkoutType,
        startedAt: Instant,
    ): Result<Unit> = mutex.withLock {
        val record = WorkoutRecord.started(workout, type, startedAt, clock.instant())
        recordRepository.startRecord(record).onSuccess { _state.value = WorkoutTrackingState(record = record) }
    }

    override suspend fun pause(): Result<Unit> = updateStatus { it.paused(clock.instant()) }

    override suspend fun resume(): Result<Unit> = updateStatus { it.resumed(clock.instant()) }

    override suspend fun addTrackPoint(location: DeviceLocation): Result<Unit> = mutex.withLock {
        val next = _state.value.nextTrackPoint(location, clock.instant()) ?: return@withLock Result.success(Unit)
        val updated = _state.value.withTrackPoint(next)
        val record = updated.record ?: return@withLock Result.success(Unit)
        recordRepository.saveTrackPoint(record, next.point).onSuccess { _state.value = updated }
    }

    override suspend fun restore(): Result<Unit> = mutex.withLock {
        suspendRunCatching {
            if (_state.value.record != null) return@suspendRunCatching
            val saved = recordRepository.getRecord().getOrThrow()
            if (saved == null || !saved.isTracking) return@suspendRunCatching
            val record = saved.restarted(clock.instant())
            recordRepository.saveRecord(record).getOrThrow()
            val trackPoints = recordRepository.getTrackPoints(record.activityId).getOrThrow()
            _state.value = WorkoutTrackingState(record = record, trackPoints = trackPoints)
        }
    }

    override suspend fun recover(): Result<Unit> = mutex.withLock {
        suspendRunCatching {
            val current = _state.value
            val saved = current.record ?: recordRepository.getRecord().getOrThrow() ?: return@suspendRunCatching
            if (!saved.isTracking) return@suspendRunCatching
            val record = saved.recovered(clock.instant())
            recordRepository.saveRecord(record).getOrThrow()
            val trackPoints = if (current.record != null) {
                current.trackPoints
            } else {
                recordRepository.getTrackPoints(record.activityId).getOrThrow()
            }
            _state.value = WorkoutTrackingState(record = record, trackPoints = trackPoints)
        }
    }

    override suspend fun finish(endedAt: Instant?): Result<Unit> = mutex.withLock {
        val record = _state.value.record ?: return@withLock Result.failure(IllegalStateException(NO_RECORD))
        val now = clock.instant()
        val finished = record.finished(now, endedAt ?: now)
        if (finished == record) return@withLock Result.success(Unit)
        recordRepository.saveRecord(finished).onSuccess { _state.value = _state.value.copy(record = finished) }
    }

    override suspend fun loadPendingUpload(): Result<Unit> = mutex.withLock {
        suspendRunCatching {
            if (_state.value.record?.isTracking == true) return@suspendRunCatching
            val saved = recordRepository.getRecord().getOrThrow()
            if (saved?.status != WorkoutStatus.PENDING_UPLOAD) return@suspendRunCatching
            val trackPoints = recordRepository.getTrackPoints(saved.activityId).getOrThrow()
            _state.value = WorkoutTrackingState(record = saved, trackPoints = trackPoints)
        }
    }

    override suspend fun clear(): Result<Unit> = mutex.withLock {
        val record = _state.value.record ?: return@withLock Result.success(Unit)
        recordRepository.deleteRecord(record.activityId).onSuccess { _state.value = WorkoutTrackingState() }
    }

    private suspend fun updateStatus(transform: (WorkoutRecord) -> WorkoutRecord): Result<Unit> {
        val changed = mutex.withLock {
            val record = _state.value.record ?: return Result.failure(IllegalStateException(NO_RECORD))
            val updated = transform(record)
            if (updated == record) return Result.success(Unit)
            recordRepository.saveRecord(updated).onFailure { return Result.failure(it) }
            _state.value = _state.value.copy(record = updated)
            updated
        }
        remoteRepository.updateStatus(changed.activityId, changed.status)
        return Result.success(Unit)
    }

    private companion object {
        const val NO_RECORD = "진행 중인 기록이 없음"
    }
}
