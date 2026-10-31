package com.routee.android.feature.workout.tracking.pause

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import java.time.Instant

/** 일시정지 2시간 알람, 부정확 알람이라 몇 분 늦을 수 있음 */
internal object WorkoutPauseAlarm {
    fun schedule(
        context: Context,
        at: Instant,
    ) {
        val alarmManager = context.getSystemService<AlarmManager>() ?: return
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toEpochMilli(), pendingIntent(context))
    }

    fun cancel(context: Context) {
        context.getSystemService<AlarmManager>()?.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, WorkoutPauseAlarmReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}
