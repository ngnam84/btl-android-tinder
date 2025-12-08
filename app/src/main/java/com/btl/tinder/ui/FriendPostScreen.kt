package com.btl.tinder.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.btl.tinder.CommonImage
import com.btl.tinder.TCViewModel
import com.btl.tinder.data.PostData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendPostScreen(navController: NavController, vm: TCViewModel) {
    val friendPosts by vm.friendPosts

    LaunchedEffect(Unit) {
        vm.fetchFriendPosts()
    }

    Scaffold(
        containerColor = Color(0xFFF0F2F5),
        topBar = {
            TopAppBar(
                title = { Text("Friend's Posts", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (vm.inProgress.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(friendPosts) { post ->
                    PostCard(post = post, vm = vm, navController = navController)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostCard(post: PostData, vm: TCViewModel, navController: NavController) {
    val currentUser = vm.userData.value
    val isLiked by remember(post.likes) { mutableStateOf(post.likes.contains(currentUser?.userId)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // --- Post Header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
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
                    Text(post.username, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    post.timestamp?.let {
                        Text(
                            text = formatTimestamp(it.time),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                IconButton(onClick = { /* TODO: Show options menu */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
            }

            // --- Post Caption ---
            post.caption?.let {
                Text(
                    text = it,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 8.dp)
                )
            }

            // --- Post Media Pager ---
            if (post.media.isNotEmpty()) {
                val pagerState = rememberPagerState { post.media.size }
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Square aspect ratio
                    .background(Color.LightGray))
                {
                    HorizontalPager(state = pagerState) {
                        val mediaItem = post.media[it]
                        Box(contentAlignment = Alignment.Center) {
                            CommonImage(
                                data = mediaItem.url,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (mediaItem.type == "video") {
                                Icon(
                                    imageVector = Icons.Filled.PlayCircle,
                                    contentDescription = "Video",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }

                    if (pagerState.pageCount > 1) {
                        Row(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            repeat(pagerState.pageCount) { iteration ->
                                val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- Action Buttons ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                IconButton(onClick = {
                    currentUser?.userId?.let { userId ->
                        vm.onLikeDislikePost(post, userId)
                    }
                }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.Gray
                    )
                }
                // Comment Button
                IconButton(onClick = { /* TODO: Navigate to comment screen */ }) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comment",
                        tint = Color.Gray
                    )
                }
                // Share Button
                IconButton(onClick = { /* TODO: Implement share logic */ }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.Gray
                    )
                }
            }

            // --- Likes and Comments Count ---
            Column(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)){
                 if(post.likes.isNotEmpty()){
                     Text("${post.likes.size} likes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                 }
                // TODO: Add comment count display
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
