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
import com.btl.tinder.DestinationScreen
import com.btl.tinder.TCViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory

@Composable
fun ChatListScreen(navController: NavController, vm: TCViewModel) {
    val context = LocalContext.current

    // Khởi tạo plugin Stream
    val offlinePluginFactory = remember {
        StreamOfflinePluginFactory(appContext = context.applicationContext)
    }
    val statePluginFactory = remember {
        StreamStatePluginFactory(config = StatePluginConfig(), appContext = context.applicationContext)
    }

    // Tạo ChatClient duy nhất
    val client = remember {
        ChatClient.Builder("asw9g2a8pkzz", context.applicationContext)
            .withPlugins(offlinePluginFactory, statePluginFactory)
            .logLevel(ChatLogLevel.ALL)
            .build()
    }

    val userData = vm.userData.value
    val firebaseUser = Firebase.auth.currentUser

    LaunchedEffect(userData, firebaseUser) {
        if (firebaseUser == null || userData?.userId == null) {
            Log.e("ChatListScreen", "❌ Firebase user or userData is null.")
            return@LaunchedEffect
        }

        Log.d("ChatListScreen", "🔄 Refreshing Firebase ID token before calling function...")

        // Làm mới ID token Firebase để đảm bảo xác thực hợp lệ
        firebaseUser.getIdToken(true)
            .addOnSuccessListener {
                Log.d("ChatListScreen", "✅ Firebase ID token refreshed successfully. Now calling Cloud Function...")

                vm.getStreamToken { streamToken ->
                    Log.d("ChatListScreen", "✅ Received Stream token from Cloud Function.")

                    val user = User(
                        id = userData.userId!!,
                        name = userData.name ?: userData.username ?: "Unknown",
                        image = userData.imageUrl ?: ""
                    )

                    client.connectUser(user, streamToken).enqueue { result ->
                        if (result.isSuccess) {
                            Log.d("ChatListScreen", "✅ Connected to Stream successfully.")
                        } else {
                            Log.e("ChatListScreen", "❌ Failed to connect to Stream: ${result.errorOrNull()?.message}")
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatListScreen", "❌ Failed to refresh Firebase token: ${e.message}")
            }
    }

    val clientInitializationState by client.clientState.initializationState.collectAsState()



    // UI
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

                    val context = LocalContext.current
                    val activity = context as? ComponentActivity

                    ChatTheme {
                        ChannelsScreen(
                            title = "Chats",
                            onChannelClick = { channel ->
                                // ✅ Mở Activity thay vì navigate
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
