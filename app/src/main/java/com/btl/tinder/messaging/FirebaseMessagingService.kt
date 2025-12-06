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
        private const val MessageReceive = "TCFCMService"
        private const val NOTIFICATION_CHANNEL_ID = "stream_chat_notifications"
        private const val NOTIFICATION_CHANNEL_NAME = "Chat Messages"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(MessageReceive, "New FCM token: $token")

        try {
            val chatClient = ChatClient.instance()
            if (chatClient.clientState.user.value != null) {
                val device = Device(
                    token = token,
                    pushProvider = PushProvider.FIREBASE,
                    providerName = "firebase_push"
                )

                chatClient.addDevice(device).enqueue { result ->
                    if (result.isSuccess) {
                        Log.d(MessageReceive, "✅ Device token registered successfully")
                    } else {
                        Log.e(MessageReceive, "❌ Failed to register token: ${result.errorOrNull()?.message}")
                    }
                }
            } else {
                Log.w(MessageReceive, "User not connected yet, token will be registered after connection")
            }
        } catch (e: Exception) {
            Log.e(MessageReceive, "Error registering token", e)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(MessageReceive, "--- NEW FCM MESSAGE RECEIVED ---")
        Log.d(MessageReceive, "From: ${message.from}")

        // ✅ Log the data payload
        if (message.data.isNotEmpty()) {
            Log.d(MessageReceive, "Data Payload: ${message.data}")
        } else {
            Log.d(MessageReceive, "Data Payload: EMPTY")
        }

        // ✅ Log the notification payload
        message.notification?.let {
            Log.d(MessageReceive, "Notification Payload: title='${it.title}', body='${it.body}'")
        } ?: run {
            Log.d(MessageReceive, "Notification Payload: EMPTY")
        }

        // --- Original Logic ---
        try {
            val isStreamNotification = message.data.containsKey("channel_id")

            if (isStreamNotification) {
                Log.d(MessageReceive, "Stream notification detected, handling it...")
                handleStreamNotification(message)
            } else {
                Log.d(MessageReceive, "This is not a Stream notification.")
            }
        } catch (e: Exception) {
            Log.e(MessageReceive, "Error handling notification", e)
        }
        Log.d(MessageReceive, "--- FCM MESSAGE PROCESSING FINISHED ---")
    }


    private fun handleStreamNotification(message: RemoteMessage) {
        val channelId = message.data["channel_id"] ?: return
        val messageText = message.data["message_text"] ?: "New message"
        val senderName = message.data["sender_name"] ?: "Someone"
        val senderImage = message.data["sender_image"]

        Log.d(MessageReceive, "Channel: $channelId, From: $senderName, Message: $messageText")

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

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("channelId", channelId)
            putExtra("openChat", true)
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
            .build()

        notificationManager.notify(channelId.hashCode(), notification)
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
        }
    }
}
