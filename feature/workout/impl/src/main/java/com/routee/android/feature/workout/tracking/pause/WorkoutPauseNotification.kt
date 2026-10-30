package com.routee.android.feature.workout.tracking.pause

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.feature.workout.R
import com.routee.android.feature.workout.tracking.WorkoutTrackingNotification
import com.routee.android.feature.workout.tracking.canPostNotifications

/** 일시정지 2시간 이어서 기록 알림, 상시 알림과 같은 채널 */
internal object WorkoutPauseNotification {
    private const val ID = 1002

    /** 알림 표시, 알림 권한이 없으면 무시 */
    fun show(context: Context) {
        if (!context.canPostNotifications()) return
        WorkoutTrackingNotification.createChannel(context)
        val notification = NotificationCompat.Builder(context, WorkoutTrackingNotification.CHANNEL_ID)
            .setSmallIcon(DesignSystemR.drawable.ic_notification_sm_white)
            .setContentTitle(context.getString(R.string.workout_pause_title))
            .setContentText(context.getString(R.string.workout_pause_text))
            .setContentIntent(WorkoutTrackingNotification.launchIntent(context))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        NotificationManagerCompat.from(context).notify(ID, notification)
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(ID)
    }
}
