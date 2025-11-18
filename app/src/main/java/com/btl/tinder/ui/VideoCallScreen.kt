package com.btl.tinder.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.ui.components.call.activecall.CallContent
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

    private val key = "asw9g2a8pkzz"
    private val secret = "fem5vds847x85vkmywrrpwnkcmznqaqgfcf5km34wjbzeafmbe8bpv2b5jjbq4ct"

    private var channelId: String? = null
    private var callStartTime: Long = 0
    private var currentCallRef: Call? = null
    private var coroutineScopeRef: kotlinx.coroutines.CoroutineScope? = null
    private var onBackPressedCallback: OnBackPressedCallback? = null
    private val isHandlingCallEnd = AtomicBoolean(false) // Flag thread-safe để tránh gọi handleCallEnd nhiều lần

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

        setContent {
            Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                VideoTheme {
                    var currentCall by remember { mutableStateOf<Call?>(null) }
                    val coroutineScope = rememberCoroutineScope()

                    LaunchedEffect(Unit) {
                        // Tạo user object
                        val user = User(
                            id = userId,
                            name = userName ?: "User",
                            image = userImage ?: ""
                        )

                        // Tạo devToken trực tiếp (chỉ dùng cho Video Call, không cần Cloud Function)
                        val devToken = StreamVideo.devToken(userId)

                        if (devToken.isNotEmpty()) {
                            try {
                                Log.d("VideoCallScreen", "🔑 Using devToken for Video Call")

                                // Kiểm tra xem đã có client chưa
                                val videoClient = try {
                                    val existingClient = StreamVideo.instance()
                                    val existingUserId = existingClient.user?.id
                                    if (existingUserId != null && existingUserId == userId) {
                                        Log.d("VideoCallScreen", "✅ Reusing existing StreamVideo client")
                                        existingClient
                                    } else {
                                        // User khác, cần tạo client mới
                                        StreamVideo.removeClient()
                                        StreamVideoBuilder(
                                            context = this@VideoCallScreen,
                                            apiKey = "ghhjw753ksej",
                                            user = user,
                                            token = devToken
                                        ).build()
                                    }
                                } catch (e: Exception) {
                                    // Chưa có client, tạo mới
                                    Log.d("VideoCallScreen", "Creating new StreamVideo client")
                                    StreamVideoBuilder(
                                        context = this@VideoCallScreen,
                                        apiKey = "ghhjw753ksej",
                                        user = user,
                                        token = devToken
                                    ).build()
                                }

                                Log.d("VideoCallScreen", "✅ StreamVideo client ready")

                                // Tạo call với client
                                val newCall = videoClient.call(type = callType, id = callId)
                                currentCall = newCall
                                currentCallRef = newCall // Lưu reference để dùng trong onBackPressed
                                coroutineScopeRef = coroutineScope // Lưu scope

                                // Join call với create = true
                                launch {
                                    try {
                                        newCall.join(create = true)
                                        Log.d("VideoCallScreen", "✅ Created and joined call")
                                        // Lưu thời gian bắt đầu khi join thành công
                                        callStartTime = System.currentTimeMillis()
                                    } catch (e: Exception) {
                                        Log.e("VideoCallScreen", "❌ Failed to join call: ${e.message}", e)
                                        finish()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("VideoCallScreen", "❌ Error setting up call: ${e.message}", e)
                                finish()
                            }
                        } else {
                            Log.e("VideoCallScreen", "❌ Failed to generate devToken")
                            finish()
                        }
                    }

                    // Hiển thị UI call
                    currentCall?.let { call ->
                        LaunchCallPermissions(call = call)

                        // Xử lý nút back và kết thúc cuộc gọi bằng OnBackPressedDispatcher
                        androidx.compose.runtime.DisposableEffect(call) {
                            // Cleanup callback cũ nếu có
                            onBackPressedCallback?.remove()

                            // Tạo callback mới cho back button
                            val callback = object : OnBackPressedCallback(true) {
                                override fun handleOnBackPressed() {
                                    Log.d("VideoCallScreen", "🔙 OnBackPressedCallback triggered")
                                    if (!isHandlingCallEnd.get()) {
                                        handleCallEnd(call, coroutineScope)
                                    } else {
                                        Log.d("VideoCallScreen", "⚠️ handleCallEnd already in progress, ignoring")
                                    }
                                }
                            }
                            // Đăng ký callback với dispatcher
                            onBackPressedDispatcher.addCallback(callback)
                            onBackPressedCallback = callback

                            // Cleanup khi DisposableEffect bị dispose
                            onDispose {
                                callback.remove()
                                if (onBackPressedCallback == callback) {
                                    onBackPressedCallback = null
                                }
                            }
                        }

                        // Xử lý nút back trong Compose (backup) - DISABLED để tránh conflict
                        // BackHandler sẽ không được gọi nếu OnBackPressedCallback đã xử lý
                        BackHandler(enabled = false) {
                            Log.d("VideoCallScreen", "🔙 BackHandler triggered (should not happen)")
                            if (!isHandlingCallEnd.get()) {
                                handleCallEnd(call, coroutineScope)
                            }
                        }

                        // Lắng nghe sự kiện khi call kết thúc hoặc connection thay đổi
                        // LƯU Ý: Không gọi handleCallEnd ở đây vì nó sẽ được gọi từ button press
                        // Chỉ log để theo dõi
                        LaunchedEffect(call) {
                            // Theo dõi connection state
                            call.state.connection.collect { connection ->
                                Log.d("VideoCallScreen", "📞 Connection state: $connection")

                                // Không gọi handleCallEnd ở đây vì sẽ được gọi từ button press
                                // Chỉ log để debug
                                if (connection is io.getstream.video.android.core.RealtimeConnection.Disconnected) {
                                    Log.d("VideoCallScreen", "📞 Call disconnected (handleCallEnd should have been called already)")
                                }
                            }
                        }

                        // Lắng nghe sự kiện khi call state thay đổi (để biết khi call kết thúc)
                        // LƯU Ý: Không gọi handleCallEnd ở đây vì nó sẽ được gọi từ button press
                        LaunchedEffect(call) {
                            try {
                                // Subscribe để lắng nghe events từ call
                                call.subscribe { event ->
                                    Log.d("VideoCallScreen", "📞 Call event: ${event::class.simpleName}")

                                    // Chỉ log events, không gọi handleCallEnd vì sẽ được gọi từ button press
                                    when (event) {
                                        is io.getstream.video.android.core.events.CallEndedSfuEvent -> {
                                            Log.d("VideoCallScreen", "📞 Call ended event received (handleCallEnd should have been called already)")
                                        }
                                        is io.getstream.android.video.generated.models.CallEndedEvent -> {
                                            Log.d("VideoCallScreen", "📞 CallEndedEvent received (handleCallEnd should have been called already)")
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("VideoCallScreen", "❌ Error subscribing to call events: ${e.message}", e)
                            }
                        }

                        // Lắng nghe khi connection thay đổi thành Disconnected (call đã kết thúc)
                        // Lưu ý: Chỉ gửi tin nhắn khi connection disconnected, không gọi handleCallEnd
                        // vì handleCallEnd đã được gọi từ onBackPressed hoặc BackHandler
                        var messageSent by remember { mutableStateOf(false) }
                        LaunchedEffect(call) {
                            call.state.connection.collect { connection ->
                                Log.d("VideoCallScreen", "📞 Connection state changed: $connection")
                                if (connection is io.getstream.video.android.core.RealtimeConnection.Disconnected && !messageSent) {
                                    Log.d("VideoCallScreen", "📞 Connection disconnected - sending message if not sent")
                                    // Chỉ gửi tin nhắn nếu chưa gửi (tránh gửi 2 lần)
                                    val durationInMs = call.state.durationInMs.value ?:
                                    (if (callStartTime > 0) System.currentTimeMillis() - callStartTime else 0)
                                    val durationText = if (durationInMs > 0) formatCallDuration(durationInMs) else "0:00"
                                    sendCallEndedMessage(durationText)
                                    messageSent = true
                                }
                            }
                        }

                        CallContent(
                            modifier = Modifier.fillMaxSize(),
                            call = call,
                            onBackPressed = {
                                Log.d("VideoCallScreen", "🔙 CallContent onBackPressed called")
                                if (!isHandlingCallEnd.get()) {
                                    handleCallEnd(call, coroutineScope)
                                } else {
                                    Log.d("VideoCallScreen", "⚠️ handleCallEnd already in progress, ignoring")
                                }
                            }
                        )
                    }
                }
            }

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("VideoCallScreen", "VideoCallScreen destroyed")
        // Cleanup callback
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
        // Cleanup
        currentCallRef = null
        coroutineScopeRef = null
    }

    private var messageSentFlag = false // Flag để tránh gửi tin nhắn 2 lần
    
    private fun handleCallEnd(call: Call, coroutineScope: kotlinx.coroutines.CoroutineScope) {
        // Kiểm tra và set flag atomically để tránh gọi nhiều lần (thread-safe)
        if (!isHandlingCallEnd.compareAndSet(false, true)) {
            Log.d("VideoCallScreen", "⚠️ handleCallEnd already in progress, ignoring duplicate call")
            return
        }
        
        Log.d("VideoCallScreen", "🔄 handleCallEnd called (first time)")
        
        // Chạy trên main thread để đảm bảo UI được cập nhật
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                Log.d("VideoCallScreen", "🔄 Handling call end...")
                
                // Lấy thời gian cuộc gọi từ CallState của Stream Video SDK
                var durationInMs = call.state.durationInMs.value
                
                Log.d("VideoCallScreen", "📞 durationInMs from state: $durationInMs")
                
                // Nếu durationInMs là null hoặc 0, thử lấy từ duration
                if (durationInMs == null || durationInMs == 0L) {
                    val duration = call.state.duration.value
                    Log.d("VideoCallScreen", "📞 duration from state: $duration")
                    if (duration != null) {
                        durationInMs = duration.inWholeSeconds * 1000
                        Log.d("VideoCallScreen", "📞 Using duration from duration.value: ${duration.inWholeSeconds}s")
                    } else {
                        // Nếu vẫn không có, tính từ thời gian bắt đầu
                        if (callStartTime > 0) {
                            durationInMs = System.currentTimeMillis() - callStartTime
                            Log.d("VideoCallScreen", "📞 Using calculated duration from start time: ${durationInMs}ms")
                        } else {
                            Log.w("VideoCallScreen", "⚠️ No duration available, using 0")
                            durationInMs = 0
                        }
                    }
                } else {
                    Log.d("VideoCallScreen", "📞 Using durationInMs from state: ${durationInMs}ms")
                }
                
                val durationText = if (durationInMs != null && durationInMs > 0) {
                    formatCallDuration(durationInMs)
                } else {
                    "0:00"
                }
                
                Log.d("VideoCallScreen", "📞 Final call duration: $durationText")
                
                // Gửi tin nhắn vào channel chat TRƯỚC khi leave (để đảm bảo gửi được)
                if (!messageSentFlag) {
                    sendCallEndedMessage(durationText)
                    messageSentFlag = true
                }
                
                // Đợi một chút để đảm bảo tin nhắn được gửi
                delay(300)
                
                // Rời cuộc gọi (có thể connection đã đóng, nên catch exception)
                try {
                    call.leave()
                    Log.d("VideoCallScreen", "✅ Left call successfully")
                } catch (e: Exception) {
                    Log.w("VideoCallScreen", "⚠️ Error leaving call (may already be disconnected): ${e.message}")
                }
                
            } catch (e: kotlinx.coroutines.CancellationException) {
                Log.w("VideoCallScreen", "⚠️ Job was cancelled: ${e.message}")
                // Vẫn cố gắng gửi tin nhắn nếu có thể
                if (!messageSentFlag) {
                    try {
                        val durationInMs = call.state.durationInMs.value ?: 
                            (if (callStartTime > 0) System.currentTimeMillis() - callStartTime else 0)
                        val durationText = if (durationInMs > 0) formatCallDuration(durationInMs) else "0:00"
                        sendCallEndedMessage(durationText)
                        messageSentFlag = true
                    } catch (ex: Exception) {
                        Log.e("VideoCallScreen", "❌ Error sending message after cancellation: ${ex.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoCallScreen", "❌ Error leaving call: ${e.message}", e)
            } finally {
                // Reset flag sau khi hoàn thành
                isHandlingCallEnd.set(false)
                // Đảm bảo finish() được gọi
                delay(200)
                if (!isFinishing && !isDestroyed) {
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
        val channelIdValue = channelId
        if (channelIdValue == null) {
            Log.w("VideoCallScreen", "⚠️ Channel ID is null, cannot send call ended message")
            return
        }

        try {
            val chatClient = ChatClient.instance()
            
            // Parse channelId để lấy channelType và channelId thực sự
            val parts = channelIdValue.split(":")
            val channelType = if (parts.size > 1) parts[0] else "messaging"
            val actualChannelId = if (parts.size > 1) parts[1] else channelIdValue
            
            // Tạo tin nhắn
            val message = Message(
                text = "Cuộc gọi đã kết thúc • $durationText"
            )
            
            // Gửi tin nhắn vào channel
            val channel = chatClient.channel(channelType, actualChannelId)
            channel.sendMessage(message).enqueue { result ->
                if (result.isSuccess) {
                    Log.d("VideoCallScreen", "✅ Call ended message sent successfully")
                } else {
                    Log.e("VideoCallScreen", "❌ Failed to send call ended message: ${result.errorOrNull()?.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("VideoCallScreen", "❌ Error sending call ended message: ${e.message}", e)
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
