package com.btl.tinder.ui

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.btl.tinder.CommonImage
import com.btl.tinder.DestinationScreen
import com.btl.tinder.TCViewModel
import com.btl.tinder.data.PostData
import com.btl.tinder.data.UserData
import com.btl.tinder.formatTimestamp
import com.btl.tinder.navigateTo
import com.btl.tinder.ui.theme.deliusFontFamily
import com.btl.tinder.ui.theme.playpenFontFamily

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(navController: NavController, vm: TCViewModel) {
    val userData = vm.userData.value
    val posts = vm.posts.value

    if (userData == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text(text = "Loading profile...", color = Color.White)
        }
        return
    }

    LaunchedEffect(key1 = userData.userId) {
        userData.userId?.let { vm.getPosts(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)) {

            item {
                ProfileHeader(userData = userData, navController = navController)
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Posts",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = playpenFontFamily
                    )
                    IconButton(onClick = {
                        navigateTo(navController, DestinationScreen.CreatePost.route)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Post",
                            tint = Color.White
                        )
                    }
                }
            }

            if (posts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No posts yet. Tap the + button to create your first post!",
                            color = Color.Gray,
                            fontFamily = playpenFontFamily,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                items(posts) { post ->
                    PostCard(
                        post = post,
                        vm = vm, 
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }
            }

            item {
                Spacer(Modifier.height(100.dp)) // Space for bottom nav
            }
        }

        TopButtons(navController)
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.PROFILE,
                navController = navController
            )
        }
    }
}


@Composable
fun ProfileHeader(userData: UserData, navController: NavController) {
    Column {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)) {
            CommonImage(
                data = userData.imageUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = userData.name ?: userData.username ?: "",
                    color = Color.White,
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = deliusFontFamily,
                    modifier = Modifier.weight(1f),
                    lineHeight = 52.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (!userData.address.isNullOrEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Location",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = userData.address!!,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = deliusFontFamily
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (!userData.bio.isNullOrEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF744D8C)),
                    border = BorderStroke(2.dp, Color(0xFF5C3D70))
                ) {
                    Text(
                        text = userData.bio!!,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = playpenFontFamily,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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

            if (userData.interests.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    userData.interests.forEach { interest ->
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
}


@Composable
private fun TopButtons(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.2f), CircleShape)
        ) {
            IconButton(onClick = {
                navigateTo(navController, DestinationScreen.EditProfileScreen.route)
            }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Edit Profile",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostCard(post: PostData, vm: TCViewModel, modifier: Modifier = Modifier) {

    var showDeleteDialog by remember { mutableStateOf(false) }
    val currentUserId = vm.userData.value?.userId

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to delete this post? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deletePost(post)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CommonImage(
                    data = post.userImage,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.username,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = playpenFontFamily
                    )
                    post.timestamp?.let {
                        Text(
                            text = formatTimestamp(it),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                if (post.userId == currentUserId) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Post", tint = Color.Gray)
                    }
                }
            }

            if (!post.caption.isNullOrEmpty()) {
                Text(
                    text = post.caption,
                    color = Color.White,
                    fontFamily = playpenFontFamily,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                )
            }

            if (post.media.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color.Black)
                ) {
                    val pagerState = rememberPagerState(pageCount = { post.media.size })
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val mediaItem = post.media[page]
                        when (mediaItem.type) {
                            "image" -> {
                                CommonImage(
                                    data = mediaItem.url,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            "video" -> {
                                VideoPlayer(url = mediaItem.url)
                            }
                            else -> {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Unsupported media type", color = Color.White)
                                }
                            }
                        }
                    }

                    // ✅ SỬA: Icon đã được xóa

                    // Chỉ báo Pager (dấu chấm)
                    if (pagerState.pageCount > 1) {
                        Row(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            repeat(pagerState.pageCount) { iteration ->
                                val color = if (pagerState.currentPage == iteration) Color.White else Color.Gray.copy(alpha = 0.5f)
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(androidx.media3.common.MediaItem.fromUri(Uri.parse(url)))
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = true
            }
        }
    )
}
