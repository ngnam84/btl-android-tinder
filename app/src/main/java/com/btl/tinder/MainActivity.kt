package com.btl.tinder

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.btl.tinder.ui.*
import com.btl.tinder.ui.theme.TinderCloneTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log

sealed class DestinationScreen(val route: String) {
    object Splash : DestinationScreen("splash")
    object Signup : DestinationScreen("signup")
    object FTSetup : DestinationScreen("ftSetup")
    object Login : DestinationScreen("login")
    object Profile : DestinationScreen("profile")
    object Swipe : DestinationScreen("swipe")
    object ChatList : DestinationScreen("chatList")
    object CreatePost : DestinationScreen("createPost")
    object EditProfileScreen : DestinationScreen("editProfile")
    object FriendPostScreen : DestinationScreen("friendPostScreen")
    object ProfileDetail : DestinationScreen("profileDetail/{userId}") {
        fun createRoute(userId: String?) = "profileDetail/$userId"
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleNotificationIntent(intent)

        setContent {
            TinderCloneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SwipeAppNavigation()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent) {
        val openChat = intent.getBooleanExtra("openChat", false)
        val channelId = intent.getStringExtra("channelId")

        if (openChat && !channelId.isNullOrEmpty()) {
            lifecycleScope.launch {
                delay(1000)
                try {
                    val formattedChannelId = if (channelId.startsWith("messaging:")) channelId else "messaging:$channelId"
                    startActivity(SingleChatScreen.getIntent(this@MainActivity, formattedChannelId))
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error opening chat", e)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SwipeAppNavigation() {
    val navController = rememberNavController()
    val vm = hiltViewModel<TCViewModel>()

    RequestNotificationPermission()

    NotificationMessage(vm = vm)

    NavHost(navController = navController, startDestination = DestinationScreen.Splash.route) {
        composable(DestinationScreen.Splash.route) {
            AnimatedSplashScreen(navController)
        }
        composable(DestinationScreen.Signup.route) {
            SignupScreen(navController, vm)
        }
        composable(DestinationScreen.FTSetup.route) {
            FTSProfileScreen(navController, vm)
        }
        composable(DestinationScreen.Login.route) {
            LoginScreen(navController, vm)
        }
        composable(DestinationScreen.Profile.route) {
            ProfileScreen(navController, vm)
        }
        composable(DestinationScreen.Swipe.route) {
            SwipeScreen(navController, vm)
        }
        composable(DestinationScreen.ChatList.route) {
            ChatListScreen(navController, vm)
        }
        composable(DestinationScreen.CreatePost.route) {
            CreatePostScreen(navController, vm)
        }
        composable(DestinationScreen.FriendPostScreen.route) {
            FriendPostScreen(navController, vm)
        }
        composable(
            route = DestinationScreen.ProfileDetail.route,
            enterTransition = {
                fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.92f, animationSpec = tween(240))
            },
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            userId?.let {
                ProfileDetailScreen(userId = it, navController = navController, vm = vm)
            }
        }
        composable(DestinationScreen.EditProfileScreen.route) {
            EditProfileScreen(navController, vm)
        }
    }
}