package com.btl.tinder.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.btl.tinder.MainActivity
import com.btl.tinder.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.PushProvider

class TCFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "TCFCMService"
        private const val NOTIFICATION_CHANNEL_ID = "stream_chat_notifications"
        private const val NOTIFICATION_CHANNEL_NAME = "Chat Messages"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🔥 FirebaseMessagingService onCreate() called")
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔥 onNewToken() called")
        Log.d(TAG, "New FCM token: $token")

        try {
            val chatClient = ChatClient.instance()
            val user = chatClient.clientState.user.value

            if (user != null) {
                val device = Device(
                    token = token,
                    pushProvider = PushProvider.FIREBASE,
                    providerName = "firebase"
                )

                chatClient.addDevice(device).enqueue { result ->
                    if (result.isSuccess) {
                        Log.d(TAG, "✅ Device token registered successfully")
                    } else {
                        Log.e(TAG, "❌ Failed to register token: ${result.errorOrNull()?.message}")
                    }
                }
            } else {
                Log.w(TAG, "⚠️ User not connected yet")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error registering token", e)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "🔥 Message received")
        Log.d(TAG, "📦 Message data: ${message.data}")

        try {
            // ✅ CHỈ HIỂN THỊ NOTIFICATION KHI APP Ở BACKGROUND
            if (isAppInForeground()) {
                Log.d(TAG, "⚠️ App is in foreground - NOT showing notification")
                return
            }

            Log.d(TAG, "✅ App is in background - showing notification")
            handleStreamNotification(message)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error handling notification", e)
        }
    }

    // ✅ HÀM MỚI: Kiểm tra app có đang chạy foreground không
    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false

        return runningAppProcesses.any { processInfo ->
            processInfo.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    processInfo.processName == packageName
        }
    }

    private fun handleStreamNotification(message: RemoteMessage) {
        var channelId = "unknown"
        var senderName = "Someone"
        var messageText = "New message"
        var senderImage: String? = null

        channelId = message.data["channel_id"]
            ?: message.data["cid"]
                    ?: channelId

        messageText = message.data["body"] ?: messageText

        message.data["title"]?.let { title ->
            senderName = if (title.startsWith("New message from ")) {
                title.removePrefix("New message from ")
            } else {
                title
            }
        }

        senderImage = message.data["sender_image"]
            ?: message.data["image"]

        Log.d(TAG, "📨 Notification: From '$senderName': $messageText")

        showNotification(
            channelId = channelId,
            title = senderName,
            message = messageText,
            senderImage = senderImage
        )
    }

    private fun showNotification(
        channelId: String,
        title: String,
        message: String,
        senderImage: String?
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val formattedChannelId = if (channelId.startsWith("messaging:")) {
            channelId
        } else {
            "messaging:$channelId"
        }

        Log.d(TAG, "Creating notification with channelId: $formattedChannelId")

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

            putExtra("channelId", formattedChannelId)
            putExtra("openChat", true)

            action = Intent.ACTION_VIEW
            data = android.net.Uri.parse("lovematch://chat/$formattedChannelId")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            channelId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.logo_app_launcher_icon)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        notificationManager.notify(channelId.hashCode(), notification)
        Log.d(TAG, "✅ Notification shown: $title")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new chat messages"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "✅ Notification channel created")
        }
    }
}