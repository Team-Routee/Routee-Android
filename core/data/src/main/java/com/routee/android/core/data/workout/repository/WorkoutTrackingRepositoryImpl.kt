package com.routee.android.core.data.workout.repository

import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.workout.model.NextTrackPoint
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import java.time.Clock
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
    private val clock: Clock,
) : WorkoutTrackingRepository {
    private val _state = MutableStateFlow(WorkoutTrackingState())
    override val state: StateFlow<WorkoutTrackingState> = _state.asStateFlow()

    private val mutex = Mutex()

    override suspend fun start(record: WorkoutRecord): Result<Unit> = mutex.withLock {
        recordRepository.startRecord(record).onSuccess { _state.value = WorkoutTrackingState(record = record) }
    }

    override suspend fun pause(): Result<Unit> = updateRecord { it.paused(clock.instant()) }

    override suspend fun resume(): Result<Unit> = updateRecord { it.resumed(clock.instant()) }

    override suspend fun addTrackPoint(next: NextTrackPoint): Result<Unit> = mutex.withLock {
        val updated = _state.value.withTrackPoint(next)
        val record = updated.record
        if (updated == _state.value || record == null) return@withLock Result.success(Unit)
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

    private suspend fun updateRecord(transform: (WorkoutRecord) -> WorkoutRecord): Result<Unit> = mutex.withLock {
        val record = _state.value.record ?: return@withLock Result.failure(IllegalStateException(NO_RECORD))
        val updated = transform(record)
        if (updated == record) return@withLock Result.success(Unit)
        recordRepository.saveRecord(updated).onSuccess { _state.value = _state.value.copy(record = updated) }
    }

    private companion object {
        const val NO_RECORD = "진행 중인 기록이 없음"
    }
}
