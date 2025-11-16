package com.btl.tinder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.btl.tinder.CommonImage
import com.btl.tinder.TCViewModel

@Composable
fun UserProfileScreen(navController: NavController, vm: TCViewModel, userId: String) {
    val user = vm.matchProfiles.value.firstOrNull { it.userId == userId }

    if (user == null) {
        // Handle case where user is not found, maybe show a loading or error message
        // For now, just pop back
        navController.popBackStack()
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Full-screen background image
        CommonImage(
            data = user.imageUrl,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Scrim gradient for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                        startY = 500f
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar with Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            // User info and action buttons at the bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = user.name ?: "",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.bio ?: "",
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dislike Button
                    Card(
                        modifier = Modifier.clip(CircleShape),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        IconButton(onClick = {
                            vm.onDislike(user)
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Dislike", tint = Color.Gray)
                        }
                    }

                    // Like Button
                    Card(
                        modifier = Modifier.clip(CircleShape),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        IconButton(onClick = {
                            vm.onLike(user)
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Rounded.Favorite, contentDescription = "Like", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}
