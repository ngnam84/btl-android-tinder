package com.btl.tinder.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

        setContent {
            ChatTheme {
                MessagesScreen(
                    viewModelFactory = MessagesViewModelFactory(
                        context = this,
                        channelId = channelId,
                        messageLimit = 30
                    ),
                    onBackPressed = { finish() }
                )
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