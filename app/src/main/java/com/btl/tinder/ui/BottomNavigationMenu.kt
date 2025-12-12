package com.btl.tinder.ui

import android.graphics.RenderEffect
import android.graphics.RenderEffect.createBlurEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.btl.tinder.DestinationScreen
import com.btl.tinder.R
import com.btl.tinder.navigateTo
import com.btl.tinder.ui.theme.TinderCloneTheme
import com.exyte.animatednavbar.AnimatedNavigationBar
import com.exyte.animatednavbar.animation.balltrajectory.Parabolic
import com.exyte.animatednavbar.animation.indendshape.Height
import com.exyte.animatednavbar.animation.indendshape.shapeCornerRadius

enum class BottomNavigationItem(val icon: Int, val navDestination: DestinationScreen, val index: Int) {
    SWIPE(R.drawable.baseline_swipe, DestinationScreen.Swipe, 0),
    FRIENDPOST(R.drawable.diversity_1_24px, DestinationScreen.FriendPostScreen, 1),
    CHATLIST(R.drawable.tooltip_2_24px, DestinationScreen.ChatList, 2),
    PROFILE(R.drawable.baseline_profile, DestinationScreen.Profile, 3)
}

@OptIn(ExperimentalGraphicsApi::class)
@Composable
fun BottomNavigationMenu(
    selectedItem: BottomNavigationItem,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 70.dp)
            .padding(horizontal = 16.dp)
            .graphicsLayer {
            }
            .background(
                color = Color.Black.copy(alpha = 0.35f),
                shape = RoundedCornerShape(40.dp)
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                RoundedCornerShape(40.dp)
            )
            .height(60.dp)
            .shadow(
                20.dp,
                RoundedCornerShape(40.dp),
                spotColor = Color.White.copy(alpha = 0.15f)
            ),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavigationItem.entries.forEach { item ->

            val isSelected = item == selectedItem

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .indication(
                        interactionSource,
                        ripple()
                    )
                    .clickable {
                        navigateTo(navController, item.navDestination.route)
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(item.icon),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    colorFilter = if (isSelected)
                        ColorFilter.tint(Color(0xFFF8F8F8))
                    else
                        ColorFilter.tint(Color.White.copy(alpha = 0.7f))
                )
            }
        }
    }
}

@Composable
fun BottomNavigationMenu1(
    selectedItem: BottomNavigationItem,
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 60.dp)
            .background(Color.Transparent)
            .graphicsLayer {
                clip = false
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(50.dp), clip = false)
                .graphicsLayer {
                    clip = false
                }
        ) {
            AnimatedNavigationBar(
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth()
                    .graphicsLayer {
                        clip = false
                    },
                selectedIndex = selectedItem.index,
                barColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.98f),
                ballColor = MaterialTheme.colorScheme.primary,
                cornerRadius = shapeCornerRadius(50.dp),
                ballAnimation = Parabolic(tween(300)),
                indentAnimation = Height(tween(300, easing = LinearEasing))
            ) {
                BottomNavigationItem.entries.forEach { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable {
                                navigateTo(navController, item.navDestination.route)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = item.icon),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            colorFilter = if (item == selectedItem)
                                ColorFilter.tint(Color.Black)
                            else
                                ColorFilter.tint(Color.White)
                        )
                    }
                }
            }
        }
    }
}