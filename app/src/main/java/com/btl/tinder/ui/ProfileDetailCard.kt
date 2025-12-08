package com.btl.tinder.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.btl.tinder.CommonImage
import com.btl.tinder.TCViewModel
import com.btl.tinder.ui.theme.deliusFontFamily
import com.btl.tinder.ui.theme.playpenFontFamily

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileDetailScreen(userId: String, navController: NavController, vm: TCViewModel) {
    val userMatch = vm.matchProfiles.value.find { it.user.userId == userId }
    val user = userMatch?.user
    val posts = vm.profileDetailPosts.value

    LaunchedEffect(key1 = userId) {
        vm.getPostsForUser(userId)
    }

    if (user == null || userMatch == null) {
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

            // User Info Section
            item {
                Column(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name ?: user.username ?: "",
                            color = Color.White,
                            fontSize = 50.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = deliusFontFamily,
                            modifier = Modifier.weight(1f),
                            lineHeight = 52.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("%.0f%%", userMatch.score * 100),
                            color = Color(0xFF2FFFBF),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.background(Color(0x80262626), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
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
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = deliusFontFamily
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    if (!user.bio.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF744D8C)),
                            border = BorderStroke(2.dp, Color(0xFF5C3D70))
                        ) {
                            Text(
                                text = user.bio!!,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontFamily = playpenFontFamily,
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Interests",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = playpenFontFamily
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (user.interests.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            user.interests.forEach { interest ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF7898)),
                                    border = BorderStroke(2.dp, Color(0xFFE06888))
                                ) {
                                    Text(
                                        text = interest,
                                        color = Color.White,
                                        fontFamily = playpenFontFamily,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Posts Title Section
            item {
                Text(
                    text = "Posts",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = playpenFontFamily,
                    modifier = Modifier.padding(start = 24.dp, end = 32.dp, top = 24.dp)
                )
            }

            // Posts Feed Section
            if (posts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${user.name ?: "This user"} hasn't posted anything yet.",
                            color = Color.Gray,
                            fontFamily = playpenFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(posts) { post ->
                    PostCard(post = post, vm, navController, modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp))
                }
            }

            item {
                Spacer(Modifier.height(50.dp))
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
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.2f), CircleShape)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.2f), CircleShape)
            ) {
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
}