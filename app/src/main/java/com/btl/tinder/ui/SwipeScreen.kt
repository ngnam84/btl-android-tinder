package com.btl.tinder.ui

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.btl.tinder.CommonImage
import com.btl.tinder.CommonProgressSpinner
import com.btl.tinder.DestinationScreen
import com.btl.tinder.R
import com.btl.tinder.TCViewModel
import com.btl.tinder.UserMatch
import com.btl.tinder.data.UserData
import com.btl.tinder.swipecards.Direction
import com.btl.tinder.swipecards.rememberSwipeableCardState
import com.btl.tinder.swipecards.swipableCard
import kotlinx.coroutines.launch
import meshGradient

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

    val profiles = vm.matchProfiles.value
    val states = profiles.map { it to rememberSwipeableCardState() }

    val animateLeftButtonTrigger = remember { mutableStateOf(0) }
    val animateRightButtonTrigger = remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) { // Main Box for overlay
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
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
            Spacer(modifier = Modifier.height(1.dp))

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
                states.forEach { (userMatch, state) ->
                    ProfileCard(
                        modifier = Modifier
                            .fillMaxSize()
                            .swipableCard(
                                state = state,
                                blockedDirections = listOf(Direction.Down),
                                onSwiped = { direction ->
                                    when (direction) {
                                        Direction.Left, Direction.Down -> {
                                            animateLeftButtonTrigger.value++
                                            vm.onDislike(userMatch.user)
                                        }
                                        Direction.Right, Direction.Up -> {
                                            animateRightButtonTrigger.value++
                                            vm.onLike(userMatch.user)
                                        }
                                        null -> {}
                                    }
                                },
                                onSwipeCancel = { Log.d("Swipeable card", "Cancelled swipe") }),
                        userMatch = userMatch,
                        navController = navController
                    )
                }
            }

            val scope = rememberCoroutineScope()
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircleButton(
                    onClick = {
                        scope.launch {
                            states.lastOrNull()?.let { (userMatch, state) ->
                                state.swipe(Direction.Left)
                                animateLeftButtonTrigger.value++
                                vm.onDislike(userMatch.user)
                            }
                        }
                    },
                    drawableResId = R.drawable.cancel,
                    backgroundColor = Color(0xFFE91E63),
                    animateTrigger = animateLeftButtonTrigger.value
                )
                CircleButton(
                    onClick = {
                        scope.launch {
                            states.lastOrNull()?.let { (userMatch, state) ->
                                state.swipe(Direction.Right)
                                animateRightButtonTrigger.value++
                                vm.onLike(userMatch.user)
                            }
                        }
                    },
                    drawableResId = R.drawable.love,
                    backgroundColor = Color(0xFF673AB7),
                    animateTrigger = animateRightButtonTrigger.value
                )
            }

            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.SWIPE,
                navController = navController
            )
        }
        if (inProgress)
            CommonProgressSpinner()
    }
}

@Composable
private fun CircleButton(
    onClick: () -> Unit,
    drawableResId: Int,
    backgroundColor: Color,
    animateTrigger: Int = 0
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(animateTrigger) {
        if (animateTrigger > 0) {
            scale.animateTo(1.2f, tween(200))
            scale.animateTo(1f, tween(200))
        }
    }

    IconButton(
        modifier = Modifier
            .scale(scale.value)
            .shadow(12.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.4f), spotColor = Color.Black.copy(alpha = 0.4f))
            .clip(CircleShape)
            .background(Color.White)
            .size(89.dp)
            .border(5.dp, backgroundColor, CircleShape),
        onClick = onClick
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

@Composable
private fun ProfileCard(
    modifier: Modifier,
    userMatch: UserMatch,
    navController: NavController
) {
    val matchProfile = userMatch.user
    Card(
        modifier
            .shadow(8.dp, RoundedCornerShape(16.dp), clip = false, ambientColor = Color.Black.copy(alpha = 0.15f), spotColor = Color.Black.copy(alpha = 0.25f))
            .clickable { navController.navigate(DestinationScreen.ProfileDetail.createRoute(matchProfile.userId)) }
    ) {
        Box {
            CommonImage(matchProfile.imageUrl, modifier = Modifier.fillMaxSize())
            Scrim(Modifier.align(Alignment.BottomCenter))
            Column(Modifier.align(Alignment.BottomStart)) {
                Row(
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = matchProfile.name ?: matchProfile.username ?: "",
                        color = Color.White,
                        fontFamily = deliusFontFamily,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format("%.0f%%", userMatch.score * 100),
                        color = Color(0xFF2FFFBF),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.background(Color(0x80262626), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }


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
                        Text(
                            text = matchProfile.address!!,
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
