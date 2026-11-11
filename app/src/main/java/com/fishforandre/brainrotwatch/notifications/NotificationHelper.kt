package com.fishforandre.brainrotwatch.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fishforandre.brainrotwatch.MainActivity
import com.fishforandre.brainrotwatch.R

object NotificationHelper {

    private const val CHANNEL_LIMIT_ALERTS = "limit_alerts_channel"
    private const val CHANNEL_COMMUNITY_ALERTS = "community_alerts_channel"
    private const val CHANNEL_SYNC_WARNINGS = "sync_warnings_channel"

    const val ACTION_TACTICAL_NUDGE = "com.fishforandre.brainrotwatch.ACTION_TACTICAL_NUDGE"
    const val ACTION_CALL_INTERVENTION = "com.fishforandre.brainrotwatch.ACTION_CALL_INTERVENTION"
    const val EXTRA_TARGET_USER_ID = "extra_target_user_id"
    const val EXTRA_TARGET_USER_NAME = "extra_target_user_name"
    const val EXTRA_TARGET_PHONE = "extra_target_phone"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val limitChannel = NotificationChannel(
                CHANNEL_LIMIT_ALERTS,
                "Screen Time Goal Alerts & Interventions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when you or a friend exceeds a daily screen-time limit with Tactical Nudge & Call actions."
            }

            val communityChannel = NotificationChannel(
                CHANNEL_COMMUNITY_ALERTS,
                "Community & Friend Requests",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts for incoming friend requests, community admission votes, and wrap-ups."
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC_WARNINGS,
                "Sync & Background Integrity Warnings",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Alerts if background screen-time sync becomes stale, goes dark, or uninstall is detected."
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(limitChannel)
            manager.createNotificationChannel(communityChannel)
            manager.createNotificationChannel(syncChannel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showThresholdAlert(context: Context, userName: String, currentMinutes: Int, limitMinutes: Int) {
        showInteractiveLimitAlert(
            context = context,
            strugglingUserName = userName,
            strugglingUserId = "user_$userName",
            phoneNumber = "5550199",
            currentMinutes = currentMinutes,
            limitMinutes = limitMinutes
        )
    }

    /**
     * Interactive Notification with two primary actions:
     * 1. Tactical Nudge: Sends an in-app visual/audio poke overlay ("Caught in 4K / Touch Grass").
     * 2. Call / Message Intervention: Launches native Intent.ACTION_DIAL / sms: deep-links and logs First Responder / Life-Line rebate eligibility.
     */
    @SuppressLint("MissingPermission")
    fun showInteractiveLimitAlert(
        context: Context,
        strugglingUserName: String,
        strugglingUserId: String,
        phoneNumber: String,
        currentMinutes: Int,
        limitMinutes: Int
    ) {
        if (!hasNotificationPermission(context)) return

        val title = "🚨 $strugglingUserName hit their limit!"
        val message = "$strugglingUserName spent $currentMinutes min today (Cap: $limitMinutes min). Call within 5m for First Responder pts & Life-Line Shield!"

        // Action 1: Tactical Nudge (opens MainActivity with nudge intent extra)
        val nudgeIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_TACTICAL_NUDGE
            putExtra(EXTRA_TARGET_USER_ID, strugglingUserId)
            putExtra(EXTRA_TARGET_USER_NAME, strugglingUserName)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val nudgePendingIntent = PendingIntent.getActivity(
            context,
            2001,
            nudgeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: Direct Call Intervention (Intent.ACTION_DIAL)
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phoneNumber.ifBlank { "5550199" }}")
        }
        val dialPendingIntent = PendingIntent.getActivity(
            context,
            2002,
            dialIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 3: SMS Message Nudge (sms: deep-link)
        val smsIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms:${phoneNumber.ifBlank { "5550199" }}")
            putExtra("sms_body", "Hey $strugglingUserName! Caught you hitting your BrainRotWatch limit — go touch some grass! 🌿")
        }
        val smsPendingIntent = PendingIntent.getActivity(
            context,
            2003,
            smsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_LIMIT_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(nudgePendingIntent)
            .addAction(0, "👀 Tactical Nudge", nudgePendingIntent)
            .addAction(0, "📞 Call (Life-Line)", dialPendingIntent)
            .addAction(0, "💬 Text Nudge", smsPendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1001, notification)
        } catch (_: SecurityException) {}
    }

    @SuppressLint("MissingPermission")
    fun showStaleSyncWarning(context: Context) {
        if (!hasNotificationPermission(context)) return

        val title = "⚠️ Stage 1: Screen Time Sync Paused"
        val message = "Your screen time isn't updating. Open the app to restore syncing before your Communities are notified (Gone Dark)."

        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC_WARNINGS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
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
