package com.routee.android.feature.workout.tracking

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.routee.android.core.data.workout.model.WorkoutRecord
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.feature.workout.R
import java.time.Instant

/** 운동 기록 중 상시 알림 */
internal object WorkoutTrackingNotification {
    const val ID = 1001
    private const val CHANNEL_ID = "workout_tracking"

    fun createChannel(context: Context) {
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(context.getString(R.string.workout_tracking_channel_name))
            .build()
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

    fun build(
        context: Context,
        record: WorkoutRecord?,
        now: Instant,
    ): Notification {
        val content = record?.toNotificationContent(now)
        val title = context.getString(content?.titleRes ?: R.string.workout_tracking_title_recording)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(DesignSystemR.drawable.ic_notification_sm_white)
            .setContentTitle(title)
            .setContentIntent(launchIntent(context))
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        if (content == null) return builder.build()

        val expanded = metricsViews(context, R.layout.notification_workout_tracking_expanded, content)
        expanded.setTextViewText(R.id.title, title)

        return builder
            .setContentText(metricsText(context, content))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(metricsViews(context, R.layout.notification_workout_tracking_collapsed, content))
            .setCustomBigContentView(expanded)
            .addAction(toggleAction(context, content))
            .build()
    }

    private fun metricsText(
        context: Context,
        content: WorkoutNotificationContent,
    ): String = context.getString(
        R.string.workout_tracking_metrics,
        content.distance,
        content.duration,
        content.maxElevation,
    )

    private fun metricsViews(
        context: Context,
        layoutRes: Int,
        content: WorkoutNotificationContent,
    ) = RemoteViews(context.packageName, layoutRes).apply {
        setTextViewText(R.id.distance, content.distance)
        setTextViewText(R.id.duration, content.duration)
        setTextViewText(R.id.max_elevation, content.maxElevation)
    }

    private fun toggleAction(
        context: Context,
        content: WorkoutNotificationContent,
    ) = NotificationCompat.Action(
        0,
        context.getString(content.actionLabelRes),
        WorkoutTrackingService.actionIntent(context, content.action),
    )

    // 앱 열기. 기록 화면(운동 중, 정지)이나 복구 모달은 운동 메인이 기록 상태로 정함
    private fun launchIntent(context: Context): PendingIntent? {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
