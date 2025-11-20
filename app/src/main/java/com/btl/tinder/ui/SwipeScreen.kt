package com.btl.tinder.ui

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.btl.tinder.CommonImage
import com.btl.tinder.CommonProgressSpinner
import com.btl.tinder.R
import com.btl.tinder.TCViewModel
import com.btl.tinder.data.UserData
import com.btl.tinder.swipecards.Direction
import com.btl.tinder.swipecards.rememberSwipeableCardState
import com.btl.tinder.swipecards.swipableCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import meshGradient
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private data class ProfileWithDistance(val userData: UserData, val distance: Double?)

@Composable
fun SwipeScreen(navController: NavController, vm: TCViewModel) {
    val inProgress = vm.inProgressProfiles.value

    val animatedPoint = remember { Animatable(.8f) }
    LaunchedEffect(Unit) {
        while (true) {
            animatedPoint.animateTo(
                targetValue = .1f,
                animationSpec = tween(durationMillis = 10000)
            )
            animatedPoint.animateTo(
                targetValue = .9f,
                animationSpec = tween(durationMillis = 10000)
            )
        }
    }

    //val profiles = vm.matchProfiles.value

    val currentUserData = vm.userData.value
    val profilesWithDistance = vm.matchProfiles.value.map {
        val distance = if (currentUserData?.lat != null && currentUserData.long != null && it.lat != null && it.long != null) {
            calculateDistance(currentUserData.lat!!, currentUserData.long!!, it.lat!!, it.long!!)
        } else {
            null
        }
        ProfileWithDistance(it, distance)
    }.sortedBy { it.distance ?: Double.MAX_VALUE }

    val states = profilesWithDistance.map { it to rememberSwipeableCardState() }

    // Triggers for left and right button animations
    val animateLeftButtonTrigger = remember { mutableStateOf(0) }
    val animateRightButtonTrigger = remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) { // Main Box for overlay
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
                .meshGradient(
                    points = listOf(
                        listOf(
                            Offset(0f, 0f) to Color.White,
                            Offset(.5f, 0f) to Color.White,
                            Offset(1f, 0f) to Color.White,
                        ),
                        listOf(
                            Offset(0f, .5f) to Color(0xFFFF7898),
                            Offset(.5f, animatedPoint.value) to Color(0xFFFF7898),
                            Offset(1f, .5f) to Color(0xFFFF7898),
                        ),
                        listOf(
                            Offset(0f, 1f) to Color(0xFF744D8C),
                            Offset(.5f, 1f) to Color(0xFF744D8C),
                            Offset(1f, 1f) to Color(0xFF744D8C),
                        ),
                    ),
                )
        ) {
            // Spacer
            Spacer(modifier = Modifier.height(1.dp))

            // Cards
            Box(
                modifier = Modifier
                    .padding(24.dp)
                    .aspectRatio(0.8f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "No more profiles available", fontFamily = deliusFontFamily, fontWeight = FontWeight.W600, color = Color.Black)
                }
                states.forEach { (profileWithDistance, state) ->
                    ProfileCard(
                        modifier = Modifier
                            .fillMaxSize()
                            .swipableCard(
                                state = state,
                                blockedDirections = listOf(Direction.Down),
                                onSwiped = { direction -> // Handle swipe direction here
                                    when (direction) {
                                        Direction.Left, Direction.Down -> {
                                            Log.d("SwipeScreen", "Card swiped Left/Down. Triggering left button animation and dislike.")
                                            animateLeftButtonTrigger.value++
                                            vm.onDislike(profileWithDistance.userData)
                                        }
                                        Direction.Right, Direction.Up -> {
                                            Log.d("SwipeScreen", "Card swiped Right/Up. Triggering right button animation and like.")
                                            animateRightButtonTrigger.value++
                                            vm.onLike(profileWithDistance.userData)
                                        }
                                        null -> { /* Should not happen with onSwiped callback */ }
                                    }
                                },
                                onSwipeCancel = { Log.d("Swipeable card", "Cancelled swipe") }),
                        matchProfile = profileWithDistance.userData,
                        distance = profileWithDistance.distance
                    )
                }
            }

            // Buttons
            val scope = rememberCoroutineScope()

            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircleButton(onClick = {
                    scope.launch {
                        val last = states.reversed().firstOrNull { it.second.offset.value == Offset(0f, 0f) }?.second
                        last?.swipe(Direction.Left)
                    }
                }, drawableResId = R.drawable.cancel, backgroundColor = Color(0xFFE91E63), animateTrigger = animateLeftButtonTrigger.value)
                CircleButton(onClick = {
                    scope.launch {
                        val last = states.reversed().firstOrNull { it.second.offset.value == Offset(0f, 0f) }?.second
                        last?.swipe(Direction.Right)
                    }
                }, drawableResId = R.drawable.love, backgroundColor = Color(0xFF673AB7), animateTrigger = animateRightButtonTrigger.value)
            }

            // Bottom nav bar
            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.SWIPE,
                navController = navController
            )
        }
        // Spinner as an overlay
        if (inProgress)
            CommonProgressSpinner()
    }
}

@Composable
private fun CircleButton(
    onClick: () -> Unit,
    drawableResId: Int,
    backgroundColor: Color,
    animateTrigger: Int = 0 // New parameter for external animation trigger
) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    // Reusable animation logic
    suspend fun playAnimation() {
        Log.d("CircleButton", "Playing animation. Scale before 1.2f: ${scale.value}")
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(durationMillis = 200) // Shorter duration for quick feedback
        )
        Log.d("CircleButton", "Scale after 1.2f: ${scale.value}")
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 200) // Shorter duration
        )
        Log.d("CircleButton", "Scale after 1f: ${scale.value}")
    }

    // LaunchedEffect để lắng nghe trigger từ bên ngoài (swipe)
    LaunchedEffect(animateTrigger) {
        if (animateTrigger > 0) { // Only trigger if value has increased (i.e., not initial 0)
            Log.d("CircleButton", "LaunchedEffect triggered with animateTrigger: $animateTrigger")
            playAnimation()
        }
    }

    Box(modifier = Modifier.scale(scale.value)) {
        IconButton(
            modifier = Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Color.Black.copy(alpha = 0.4f)
                )
                .clip(CircleShape)
                .background(Color.White)
                .size(89.dp)
                .border(5.dp, backgroundColor, CircleShape),
            onClick = {
                // Handle direct button click
                scope.launch {
                    playAnimation()
                    onClick.invoke() // Call original onClick after button's own animation
                }
            }
        ) {
            Image(
                painter = painterResource(id = drawableResId),
                contentDescription = "Button icon",
                modifier = Modifier.aspectRatio(0.5f, true),
                alignment = Alignment.Center,
                contentScale = ContentScale.Inside,
            )
        }
    }
}

@Composable
private fun ProfileCard(
    modifier: Modifier,
    matchProfile: UserData,
    distance: Double?
) {
    Card(
        modifier
            .shadow(
                elevation = 8.dp,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp),
                clip = false
            )
    ) {
        Box {
            CommonImage(matchProfile.imageUrl, modifier = Modifier.fillMaxSize())
            Scrim(Modifier.align(Alignment.BottomCenter))
            Column(Modifier.align(Alignment.BottomStart)) {
                Text(
                    text = matchProfile.name ?: matchProfile.username ?: "",
                    color = Color.White,
                    fontFamily = deliusFontFamily,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 10.dp)
                )

                if (!matchProfile.address.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Location",
                            tint = Color(0xFF8948A2),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val locationText = buildString {
                            append(matchProfile.address!!)
                            distance?.let {
                                if (it > 50) {
                                    append(" (${String.format("%.1f", it)} km away)")
                                }
                            }
                        }
                        Text(
                            text = locationText,
                            color = Color.White,
                            fontFamily = deliusFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                if (!matchProfile.bio.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bookmarks,
                            contentDescription = "Bio",
                            tint = Color(0xFF8948A2),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = matchProfile.bio!!,
                            color = Color.White,
                            fontFamily = playpenFontFamily,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Scrim(modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            .height(180.dp)
            .fillMaxWidth())}

private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusKm * c
}
