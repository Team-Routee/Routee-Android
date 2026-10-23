package com.routee.android.feature.workout.tracking

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import com.routee.android.core.domain.workout.TrackWorkoutUseCase
import dagger.hilt.android.AndroidEntryPoint
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber

/** 운동 기록 중 위치를 받는 포그라운드 서비스, 상시 알림 표시 */
@AndroidEntryPoint
internal class WorkoutTrackingService : Service() {
    @Inject
    lateinit var trackingRepository: WorkoutTrackingRepository

    @Inject
    lateinit var trackWorkout: TrackWorkoutUseCase

    @Inject
    lateinit var clock: Clock

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var trackingJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (!startForeground()) {
            stopSelf()
            return START_NOT_STICKY
        }
        when (intent?.action) {
            ACTION_PAUSE -> scope.launch {
                trackingRepository.restore()
                trackingRepository.pause()
            }

            ACTION_RESUME -> scope.launch {
                trackingRepository.restore()
                trackingRepository.resume()
            }
        }
        if (trackingJob == null) {
            trackingJob = scope.launch {
                trackWorkout(
                    onTrackPointFailure = { Timber.e(it, "경로 좌표 저장 실패, 기록 계속") },
                ).onFailure { Timber.e(it, "운동 기록 실패, 서비스 종료") }
                trackingJob = null
                stopSelfResult(startId)
            }
            scope.launch { updateNotification() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun startForeground(): Boolean {
        WorkoutTrackingNotification.createChannel(this)
        val notification = WorkoutTrackingNotification.build(
            this,
            trackingRepository.state.value.record,
            clock.instant(),
        )
        return runCatching {
            ServiceCompat.startForeground(this, WorkoutTrackingNotification.ID, notification, FOREGROUND_SERVICE_TYPE)
        }.onFailure(Timber::e).isSuccess
    }

    private suspend fun updateNotification() {
        val minuteTicks = flow {
            while (true) {
                emit(Unit)
                delay(NOTIFICATION_TICK_MILLIS)
            }
        }
        trackingRepository.state.combine(minuteTicks) { state, _ -> state.record }.collect { record ->
            val isDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            if (isDenied) return@collect
            NotificationManagerCompat.from(this).notify(
                WorkoutTrackingNotification.ID,
                WorkoutTrackingNotification.build(this, record, clock.instant()),
            )
        }
    }

    companion object {
        const val ACTION_PAUSE = "com.routee.android.workout.action.PAUSE"
        const val ACTION_RESUME = "com.routee.android.workout.action.RESUME"
        private const val NOTIFICATION_TICK_MILLIS = 60_000L
        private val FOREGROUND_SERVICE_TYPE =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0

        /** 위치 기록 시작, 위치 권한과 시작한 기록이 있어야 함 (기록이 없으면 바로 멈춤) */
        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, WorkoutTrackingService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WorkoutTrackingService::class.java))
        }

        fun actionIntent(
            context: Context,
            action: String,
        ): PendingIntent = PendingIntent.getService(
            context,
            action.hashCode(),
            Intent(context, WorkoutTrackingService::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
