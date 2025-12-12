package com.btl.tinder.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

class SingleChatScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val channelId = intent.getStringExtra(KEY_CHANNEL_ID)
        if (channelId == null) {
            finish()
            return
        }

        val activityContext = this
        val firebaseUser = Firebase.auth.currentUser
        val userId = firebaseUser?.uid ?: ""
        val userName = firebaseUser?.displayName ?: "User"

        val callId = channelId.replace("messaging:", "").replace(":", "_")

        setContent {

            val systemUiController = rememberSystemUiController()

            SideEffect {
                systemUiController.setStatusBarColor(
                    color = Color.Transparent,
                    darkIcons = true
                )
                systemUiController.setNavigationBarColor(
                    color = Color.Transparent
                )
            }

            ChatTheme {

                Box(modifier = Modifier.fillMaxSize()) {
                    // MessagesScreen mặc định
                    MessagesScreen(
                        viewModelFactory = MessagesViewModelFactory(
                            context = activityContext,
                            channelId = channelId,
                            messageLimit = 30
                        ),
                        onBackPressed = { finish() }
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(top = 7.dp, end = 50.dp)
                    ) {
                        VideoCallButton(
                            onVideoCallClick = {
                                // Mở màn hình video call
                                if (userId.isNotEmpty()) {
                                    startActivity(
                                        VideoCallScreen.getIntent(
                                            context = activityContext,
                                            callId = callId,
                                            userId = userId,
                                            userName = userName,
                                            channelId = channelId
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"

        fun getIntent(context: Context, channelId: String): Intent {
            return Intent(context, SingleChatScreen::class.java).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
            }
        }
    }
}

@Composable
fun VideoCallButton(
    onVideoCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onVideoCallClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Videocam,
            contentDescription = "Video Call",
            modifier = Modifier.size(40.dp)
        )
    }
}