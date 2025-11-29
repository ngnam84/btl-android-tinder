package com.btl.tinder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun ProfileDetailScreen(userId: String, navController: NavController, vm: TCViewModel) {
    val userMatch = vm.matchProfiles.value.find { it.user.userId == userId }
    val user = userMatch?.user

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text(text = "User not found.", color = Color.White)
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Back", tint = Color.White)
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Image with Gradient
            item {
                Box(modifier = Modifier
                    .fillParentMaxWidth()
                    .height(450.dp)) {
                    CommonImage(
                        data = user.imageUrl,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient scrim
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black),
                                startY = 400f
                            )
                        )
                    )
                }
            }

            // User Info
            item {
                Column(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = user.name ?: user.username ?: "",
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (!user.address.isNullOrEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Location",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = user.address!!,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    if (!user.bio.isNullOrEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Bookmarks,
                                contentDescription = "Bio",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = user.bio!!,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Spacer for scrollability
                    Spacer(Modifier.height(500.dp))
                }
            }
        }

        // Top buttons, over everything
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = { /* TODO: More options */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
