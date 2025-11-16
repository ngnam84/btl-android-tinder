package com.btl.tinder.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.btl.tinder.DestinationScreen
import com.btl.tinder.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(navController: NavController) {

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

    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 3000
        )
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
        navController.popBackStack()
        navController.navigate(DestinationScreen.Signup.route)
    }
    Splash(alpha = alphaAnim.value)
}

@Composable
fun Splash(alpha: Float) {
    Box(
        modifier = Modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White,
                        Color.White,
                        Color(0xFFFFC1CC),
                        Color(0xFFD1C4E9),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 2500f)
                )
            )
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
//        Icon(
//            modifier = Modifier
//                .size(120.dp)
//                .alpha(alpha = alpha),
//            imageVector = Icons.Default.Email,
//            contentDescription = "Logo Icon",
//            tint = Color.White
//        )
        Image(
            painter = painterResource(R.drawable.logo_sub_1_fixed),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.7f)
        )
    }
}

@Composable
@Preview
fun SplashScreenPreview() {
    Splash(alpha = 1f)
}

@Composable
@Preview(uiMode = UI_MODE_NIGHT_YES)
fun SplashScreenDarkPreview() {
    Splash(alpha = 1f)
}