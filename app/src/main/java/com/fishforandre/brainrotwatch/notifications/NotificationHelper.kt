package com.fishforandre.brainrotwatch.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fishforandre.brainrotwatch.R

object NotificationHelper {

    private const val CHANNEL_LIMIT_ALERTS = "limit_alerts_channel"
    private const val CHANNEL_COMMUNITY_ALERTS = "community_alerts_channel"
    private const val CHANNEL_SYNC_WARNINGS = "sync_warnings_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val limitChannel = NotificationChannel(
                CHANNEL_LIMIT_ALERTS,
                "Screen Time Goal Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when you or a friend exceeds a daily screen-time limit."
            }

            val communityChannel = NotificationChannel(
                CHANNEL_COMMUNITY_ALERTS,
                "Community & Friend Requests",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts for incoming friend requests and community admission votes."
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC_WARNINGS,
                "Sync & Background Warnings",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Alerts if background screen-time sync becomes stale or inactive."
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(limitChannel)
            manager.createNotificationChannel(communityChannel)
            manager.createNotificationChannel(syncChannel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showThresholdAlert(context: Context, userName: String, currentMinutes: Int, limitMinutes: Int) {
        if (!hasNotificationPermission(context)) return

        val title = "⚠️ Screen Time Goal Exceeded"
        val message = "$userName has spent $currentMinutes min on screen today (Limit: $limitMinutes min)."

        val notification = NotificationCompat.Builder(context, CHANNEL_LIMIT_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1001, notification)
        } catch (_: SecurityException) {}
    }

    @SuppressLint("MissingPermission")
    fun showStaleSyncWarning(context: Context) {
        if (!hasNotificationPermission(context)) return

        val title = "🔄 Screen Time Sync Paused"
        val message = "Your background screen-time sync hasn't updated in 45 minutes. Open BrainRotWatch to keep your community score accurate."

        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC_WARNINGS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1002, notification)
        } catch (_: SecurityException) {}
    }

    @SuppressLint("MissingPermission")
    fun showFriendRequestAlert(context: Context, senderName: String) {
        if (!hasNotificationPermission(context)) return

        val title = "👥 New Friend Request"
        val message = "$senderName sent you a friend request on BrainRotWatch."

        val notification = NotificationCompat.Builder(context, CHANNEL_COMMUNITY_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1003, notification)
        } catch (_: SecurityException) {}
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
