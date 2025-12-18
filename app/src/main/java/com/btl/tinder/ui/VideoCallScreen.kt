package com.btl.tinder.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PictureInPictureParams
import androidx.compose.foundation.layout.systemBarsPadding
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import kotlinx.coroutines.launch
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.ui.components.call.activecall.CallContent
import io.getstream.video.android.core.call.state.*
import io.getstream.video.android.compose.permission.LaunchCallPermissions
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.model.User
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Message
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class VideoCallScreen : ComponentActivity() {

    private var channelId: String? = null
    private var callStartTime: Long = 0
    private val isHandlingCallEnd = AtomicBoolean(false)
    private var messageSentFlag = false

    private var isInPipMode = false
    private var currentCall: Call? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callId = intent.getStringExtra(KEY_CALL_ID)
        val callType = intent.getStringExtra(KEY_CALL_TYPE) ?: "default"
        val userId = intent.getStringExtra(KEY_USER_ID)
        val userName = intent.getStringExtra(KEY_USER_NAME)
        val userImage = intent.getStringExtra(KEY_USER_IMAGE)
        channelId = intent.getStringExtra(KEY_CHANNEL_ID)

        if (callId == null || userId == null) {
            finish()
            return
        }

        setupBackPressedHandler()

        setContent {
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.setDecorFitsSystemWindows(window, false) // Thêm dòng này
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                }
            }

            Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) { // Thay đổi từ statusBarsPadding()
                VideoTheme {
                    var call by remember { mutableStateOf<Call?>(null) }
                    val coroutineScope = rememberCoroutineScope()

                    LaunchedEffect(Unit) {
                        val user = User(
                            id = userId,
                            name = userName ?: "User",
                            image = userImage ?: ""
                        )

                        val devToken = StreamVideo.devToken(userId)

                        if (devToken.isNotEmpty()) {
                            try {
                                val videoClient = try {
                                    val existingClient = StreamVideo.instance()
                                    val existingUserId = existingClient.user?.id

                                    if (existingUserId != null && existingUserId == userId) {
                                        existingClient
                                    } else {
                                        StreamVideo.removeClient()
                                        StreamVideoBuilder(
                                            context = this@VideoCallScreen,
                                            apiKey = "ghhjw753ksej",
                                            user = user,
                                            token = devToken
                                        ).build()
                                    }
                                } catch (e: Exception) {
                                    StreamVideoBuilder(
                                        context = this@VideoCallScreen,
                                        apiKey = "ghhjw753ksej",
                                        user = user,
                                        token = devToken
                                    ).build()
                                }

                                val newCall = videoClient.call(type = callType, id = callId)
                                call = newCall
                                currentCall = newCall

                                launch {
                                    try {
                                        newCall.join(create = true)
                                        callStartTime = System.currentTimeMillis()
                                        Log.d("VideoCallScreen", "Joined call successfully")
                                    } catch (e: Exception) {
                                        Log.e("VideoCallScreen", "Failed to join call: ${e.message}")
                                        finish()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("VideoCallScreen", "Setup error: ${e.message}")
                                finish()
                            }
                        } else {
                            finish()
                        }
                    }

                    call?.let { activeCall ->
                        LaunchCallPermissions(call = activeCall)

                        LaunchedEffect(activeCall) {
                            var wasConnected = false

                            activeCall.state.connection.collect { connection ->
                                if (connection is io.getstream.video.android.core.RealtimeConnection.Connected) {
                                    wasConnected = true
                                    Log.d("VideoCallScreen", "Connection: CONNECTED")
                                }

                                if (connection is io.getstream.video.android.core.RealtimeConnection.Disconnected && wasConnected) {
                                    Log.d("VideoCallScreen", "Connection: DISCONNECTED")
                                    if (!isHandlingCallEnd.get() && !messageSentFlag) {
                                        Log.d("VideoCallScreen", "Calling handleCallEnd from connection listener")
                                        handleCallEnd(activeCall, coroutineScope)
                                    }
                                }
                            }
                        }

                        LaunchedEffect(activeCall) {
                            try {
                                activeCall.subscribe { event ->
                                    Log.d("VideoCallScreen", "Event: ${event::class.simpleName}")

                                    when (event) {
                                        is io.getstream.video.android.core.events.CallEndedSfuEvent -> {
                                            if (!isHandlingCallEnd.get() && !messageSentFlag) {
                                                handleCallEnd(activeCall, coroutineScope)
                                            }
                                        }
                                        is io.getstream.android.video.generated.models.CallEndedEvent -> {
                                            if (!isHandlingCallEnd.get() && !messageSentFlag) {
                                                handleCallEnd(activeCall, coroutineScope)
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("VideoCallScreen", "Subscribe error: ${e.message}", e)
                            }
                        }

                        CallContent(
                            modifier = Modifier.fillMaxSize(),
                            call = activeCall,
                            onBackPressed = {
                                Log.d("VideoCallScreen", "Back pressed - entering PiP mode")
                                enterPipMode()
                            },
                            onCallAction = { action ->
                                when (action) {
                                    is LeaveCall -> {
                                        Log.d("VideoCallScreen", "LeaveCall action - ending call")
                                        handleCallEnd(activeCall, coroutineScope)
                                    }
                                    is ToggleCamera -> {
                                        activeCall.camera.setEnabled(action.isEnabled)
                                    }
                                    is ToggleMicrophone -> {
                                        activeCall.microphone.setEnabled(action.isEnabled)
                                    }
                                    is FlipCamera -> {
                                        activeCall.camera.flip()
                                    }
                                    else -> Unit
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        Log.d("VideoCallScreen", "User leave hint - entering PiP")
        enterPipMode()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode

        if (isInPictureInPictureMode) {
            Log.d("VideoCallScreen", "Entered PiP mode")
        } else {
            Log.d("VideoCallScreen", "Exited PiP mode")

            if (!isFinishing) {
            }
        }
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                try {
                    val params = PictureInPictureParams.Builder()
                        .setAspectRatio(Rational(16, 9))
                        .build()

                    val result = enterPictureInPictureMode(params)

                    if (result) {
                        Log.d("VideoCallScreen", "Successfully entered PiP mode")
                    } else {
                        Log.w("VideoCallScreen", "Failed to enter PiP mode")
                    }
                } catch (e: Exception) {
                    Log.e("VideoCallScreen", "PiP error: ${e.message}", e)
                }
            }
        } else {
            Log.w("VideoCallScreen", "PiP requires Android O (API 26) or higher")
        }
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isInPipMode) {
                    Log.d("VideoCallScreen", "🔙 Back pressed in PiP - ignoring")
                } else {
                    Log.d("VideoCallScreen", "🔙 System back pressed - entering PiP")
                    enterPipMode()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("VideoCallScreen", "💀 onDestroy called")

        if (!isHandlingCallEnd.get() && !messageSentFlag) {
            currentCall?.let { call ->
                try {
                    call.leave()
                    Log.d("VideoCallScreen", "Call left in onDestroy")
                } catch (e: Exception) {
                    Log.e("VideoCallScreen", "Error leaving call in onDestroy: ${e.message}")
                }
            }
        }
    }

    private fun handleCallEnd(call: Call, coroutineScope: kotlinx.coroutines.CoroutineScope) {
        Log.d("VideoCallScreen", "handleCallEnd() called")

        if (!isHandlingCallEnd.compareAndSet(false, true)) {
            Log.w("VideoCallScreen", "handleCallEnd() already in progress")
            return
        }

        Log.d("VideoCallScreen", "handleCallEnd() started")

        coroutineScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                var durationInMs = call.state.durationInMs.value
                if (durationInMs == null || durationInMs == 0L) {
                    val duration = call.state.duration.value
                    if (duration != null) {
                        durationInMs = duration.inWholeSeconds * 1000
                    } else {
                        if (callStartTime > 0) {
                            durationInMs = System.currentTimeMillis() - callStartTime
                        }
                        else {
                            durationInMs = 0
                        }
                    }
                }

                if (durationInMs < 0) {
                    durationInMs = 0L
                }

                val durationText = if (durationInMs > 0) {
                    formatCallDuration(durationInMs)
                } else {
                    "0:00"
                }

                if (!messageSentFlag) {
                    Log.d("VideoCallScreen", "📤 Sending message: $durationText")
                    sendCallEndedMessage(durationText)
                    messageSentFlag = true
                }

                try {
                    val connection = call.state.connection.value
                    if (connection !is io.getstream.video.android.core.RealtimeConnection.Disconnected) {
                        Log.d("VideoCallScreen", "Leaving call")
                        call.leave()
                    }
                } catch (e: Exception) {
                    Log.w("VideoCallScreen", "Leave error: ${e.message}")
                }

            } catch (e: Exception) {
                Log.e("VideoCallScreen", "handleCallEnd error: ${e.message}", e)
            } finally {
                isHandlingCallEnd.set(false)

                if (!isFinishing && !isDestroyed) {
                    Log.d("VideoCallScreen", "Finishing activity")
                    finish()
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatCallDuration(durationMs: Long): String {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)

        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%d:%02d", minutes, seconds)
            else -> String.format("0:%02d", seconds)
        }
    }

    private fun sendCallEndedMessage(durationText: String) {
        val channelIdValue = channelId ?: return

        try {
            val chatClient = ChatClient.instance()
            val parts = channelIdValue.split(":")
            val channelType = if (parts.size > 1) parts[0] else "messaging"
            val actualChannelId = if (parts.size > 1) parts[1] else channelIdValue

            val message = Message(text = "Cuộc gọi đã kết thúc • $durationText")
            val channel = chatClient.channel(channelType, actualChannelId)

            channel.sendMessage(message).enqueue { result ->
                if (result.isSuccess) {
                    Log.d("VideoCallScreen", "Message sent successfully")
                } else {
                    Log.e("VideoCallScreen", "Failed to send message: ${result.errorOrNull()?.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("VideoCallScreen", "Send message error: ${e.message}", e)
        }
    }

    companion object {
        private const val KEY_CALL_ID = "callId"
        private const val KEY_CALL_TYPE = "callType"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_IMAGE = "userImage"
        private const val KEY_CHANNEL_ID = "channelId"

        fun getIntent(
            context: Context,
            callId: String,
            callType: String = "default",
            userId: String,
            userName: String? = null,
            userImage: String? = null,
            channelId: String? = null
        ): Intent {
            return Intent(context, VideoCallScreen::class.java).apply {
                putExtra(KEY_CALL_ID, callId)
                putExtra(KEY_CALL_TYPE, callType)
                putExtra(KEY_USER_ID, userId)
                putExtra(KEY_USER_NAME, userName)
                putExtra(KEY_USER_IMAGE, userImage)
                putExtra(KEY_CHANNEL_ID, channelId)
            }
        }
    }
}
