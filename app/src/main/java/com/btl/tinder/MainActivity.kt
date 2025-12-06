package com.btl.tinder

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.btl.tinder.ui.AnimatedSplashScreen
import com.btl.tinder.ui.ChatListScreen
import com.btl.tinder.ui.CreatePostScreen
import com.btl.tinder.ui.EditProfileScreen
import com.btl.tinder.ui.FTSProfileScreen
import com.btl.tinder.ui.LoginScreen
import com.btl.tinder.ui.ProfileDetailScreen
import com.btl.tinder.ui.ProfileScreen
import com.btl.tinder.ui.SignupScreen
import com.btl.tinder.ui.SwipeScreen
import com.btl.tinder.ui.theme.TinderCloneTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class DestinationScreen(val route: String) {
    object Splash : DestinationScreen("splash")
    object Signup : DestinationScreen("signup")
    object FTSetup : DestinationScreen("ftsetup")
    object Login : DestinationScreen("login")
    object Profile : DestinationScreen("profile")
    object Swipe : DestinationScreen("swipe")
    object ChatList : DestinationScreen("chatList")
    object CreatePost : DestinationScreen("createPost")
    object EditProfileScreen : DestinationScreen("editProfile")
    object ProfileDetail : DestinationScreen("profileDetail/{userId}") {
        fun createRoute(userId: String?) = "profileDetail/$userId"
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SwipeAppNavigation() {
    val navController = rememberNavController()
    val vm = hiltViewModel<TCViewModel>()

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
            ProfileScreen(navController,vm)
        }

        composable(DestinationScreen.Swipe.route) {
            SwipeScreen(navController, vm)
        }

        composable(DestinationScreen.ChatList.route) {
            ChatListScreen(navController,vm)
        }

        composable(DestinationScreen.CreatePost.route) {
            CreatePostScreen(navController, vm)
        }

        composable(
            route = DestinationScreen.ProfileDetail.route,
            enterTransition = {
                fadeIn(animationSpec = tween(200)) +
                        scaleIn(
                            initialScale = 0.92f,
                            animationSpec = tween(240)
                        )
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