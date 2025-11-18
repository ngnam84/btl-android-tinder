package com.btl.tinder.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
    CHATLIST(R.drawable.baseline_chat, DestinationScreen.ChatList, 1),
    PROFILE(R.drawable.baseline_profile, DestinationScreen.Profile, 2)
}

@Composable
fun BottomNavigationMenu(selectedItem: BottomNavigationItem, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
//            .wrapContentHeight()
//            .padding(top = 4.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color.White)
            .shadow(100.dp, RoundedCornerShape(50.dp), clip = false)
            .clip(RoundedCornerShape(50.dp)),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (item in BottomNavigationItem.entries) {
            Image(
                painter = painterResource(id = item.icon),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .padding(top = 10.dp, bottom = 10.dp, start = 4.dp, end = 4.dp)
                    .weight(1f)
                    .clickable {
                        navigateTo(navController, item.navDestination.route)
                    },
                colorFilter = if (item == selectedItem) ColorFilter.tint(Color(0xFF5E35B1))
                else ColorFilter.tint(Color.Black)
            )
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



