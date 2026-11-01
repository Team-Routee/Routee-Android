package com.routee.android.feature.workout.tracking.pause

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.routee.android.core.data.workout.model.UnfinishedWorkout
import com.routee.android.core.domain.workout.GetUnfinishedWorkoutUseCase
import com.routee.android.feature.workout.tracking.WorkoutTrackingService
import dagger.hilt.android.AndroidEntryPoint
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/** 일시정지 2시간 알람 수신, 이어서 기록 알림 표시와 위치 기록 서비스 종료 */
@AndroidEntryPoint
internal class WorkoutPauseAlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var getUnfinishedWorkout: GetUnfinishedWorkoutUseCase

    @Inject
    lateinit var clock: Clock

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Main.immediate).launch {
            try {
                if (isPauseExpired()) notifyPauseExpired(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun isPauseExpired(): Boolean {
        val unfinished = getUnfinishedWorkout().onFailure { Timber.e(it, "일시정지 기록 조회 실패") }.getOrNull()
        return (unfinished as? UnfinishedWorkout.Recoverable)?.record?.isPauseExpiredAt(clock.instant()) == true
    }

    private fun notifyPauseExpired(context: Context) {
        WorkoutTrackingService.stop(context)
        WorkoutPauseNotification.show(context)
    }
}
