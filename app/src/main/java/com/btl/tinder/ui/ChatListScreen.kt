package com.btl.tinder.ui

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.btl.tinder.TCViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User

@Composable
fun ChatListScreen(navController: NavController, vm: TCViewModel) {
    val context = LocalContext.current


    val client = vm.chatClient

    val userData = vm.userData.value
    val firebaseUser = Firebase.auth.currentUser

    LaunchedEffect(userData, firebaseUser) {
        if (firebaseUser == null || userData?.userId == null) {
            Log.e("ChatListScreen", "❌ Firebase user or userData is null.")
            return@LaunchedEffect
        }

        // Kiểm tra xem đã connect chưa
        val currentUser = client.clientState.user.value
        if (currentUser != null && currentUser.id == userData.userId) {
            Log.d("ChatListScreen", "✅ Already connected to Stream")
            return@LaunchedEffect
        }

        Log.d("ChatListScreen", "🔄 Connecting to Stream...")

        firebaseUser.getIdToken(true)
            .addOnSuccessListener {
                vm.getStreamToken { streamToken ->
                    val user = User(
                        id = userData.userId!!,
                        name = userData.name ?: userData.username ?: "Unknown",
                        image = userData.imageUrl ?: ""
                    )

                    client.connectUser(user, streamToken).enqueue { result ->
                        if (result.isSuccess) {
                            Log.d("ChatListScreen", "✅ Connected to Stream successfully")
                        } else {
                            Log.e("ChatListScreen", "❌ Failed to connect: ${result.errorOrNull()?.message}")
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatListScreen", "❌ Token refresh failed: ${e.message}")
            }
    }

    val clientInitializationState by client.clientState.initializationState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp)
        ) {
            when (clientInitializationState) {
                InitializationState.COMPLETE -> {
                    ChatTheme {
                        ChannelsScreen(
//                             title = "Chats",
//                             onChannelClick = { channel: Channel ->
                                // ✅ Mở Activity thay vì navigate
                            title = "Matches",
                            onChannelClick = { channel ->
                                context.startActivity(
                                    SingleChatScreen.getIntent(context, channel.cid)
                                )
                            },
                            onBackPressed = { navController.popBackStack() }
                        )
                    }
                }

                InitializationState.INITIALIZING -> {
                    Text(text = "Connecting to chat...")
                }

                InitializationState.NOT_INITIALIZED -> {
                    Text(text = "Waiting for authentication...")
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.CHATLIST,
                navController = navController
            )
        }
    }
}