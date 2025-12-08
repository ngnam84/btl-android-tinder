package com.btl.tinder.ui

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.btl.tinder.data.CommentData
import com.btl.tinder.data.PostData
import com.btl.tinder.data.UserData
import com.btl.tinder.formatTimestamp
import com.btl.tinder.navigateTo
import com.btl.tinder.ui.theme.playpenFontFamily
import meshGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendPostScreen(navController: NavController, vm: TCViewModel) {
    val friendPosts by vm.friendPosts
    val inProgress = vm.inProgress.value

    LaunchedEffect(Unit) {
        vm.fetchFriendPosts()
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
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
                .statusBarsPadding()
        ) {
            if (inProgress && friendPosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                // Horizontal list of friend avatars
                val uniqueFriends = remember(friendPosts) {
                    friendPosts
                        .map { UserData(userId = it.userId, username = it.username, imageUrl = it.userImage) }
                        .distinctBy { it.userId }
                }

                if (uniqueFriends.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(uniqueFriends, key = { it.userId!! }) { friend ->
                            FriendAvatar(friend = friend)
                        }
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
                ) {
                    items(friendPosts, key = { it.postId!! }) { post ->
                        PostCard(
                            post = post,
                            vm = vm,
                            navController = navController,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.FRIENDPOST,
                navController = navController
            )
        }
    }
}

@Composable
fun FriendAvatar(friend: UserData) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        CommonImage(
            data = friend.imageUrl,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.Gray) // Placeholder background
                .border(2.dp, Color.White, CircleShape)
                .shadow(4.dp, CircleShape)
        )
        // Removed Text for username
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostCard(
    post: PostData,
    vm: TCViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val currentUser = vm.userData.value
    val isLiked = post.likes.contains(currentUser?.userId)
    var showDeleteDialog by remember { mutableStateOf(false) }

    val comments by vm.getCommentsFlow(post.postId!!).collectAsState(initial = emptyList())
    var commentInput by remember { mutableStateOf("") }


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

                if (post.userId == currentUser?.userId) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options", tint = Color.Gray)
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
                    ) {
                        val mediaItem = post.media[it]
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                IconButton(onClick = { /* Comment section is always shown now, no toggle needed */ }) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comment",
                        tint = Color.Gray
                    )
                }
            }

            if (post.likes.isNotEmpty()) {
                val likesCount = post.likes.size
                Text(
                    text = "$likesCount ${if (likesCount > 1) "likes" else "like"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
                )
            }

            // Always show the comment section
            CommentsSection(
                postId = post.postId!!,
                comments = comments,
                vm = vm,
                commentInput = commentInput,
                onCommentInputChange = { commentInput = it },
                onCommentSend = {
                    vm.postComment(post.postId, commentInput)
                    commentInput = ""
                }
            )
        }
    }
}

@Composable
fun VideoPlayer(url: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(url)))
            prepare()
            playWhenReady = false // Autoplay can be set to true if desired
        }
    }

    DisposableEffect(
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                }
            }
        )
    ) {
        onDispose {
            exoPlayer.release()
        }
    }
}

@Composable
fun CommentsSection(
    postId: String,
    comments: List<CommentData>,
    vm: TCViewModel,
    commentInput: String,
    onCommentInputChange: (String) -> Unit,
    onCommentSend: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(
            text = "Comments",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            androidx.compose.material3.OutlinedTextField(
                value = commentInput,
                onValueChange = onCommentInputChange,
                label = { Text("Add a comment...", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Removed Share IconButton
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (comments.isEmpty()) {
            Text(text = "No comments yet.", color = Color.Gray, fontSize = 14.sp)
        } else {
            comments.forEach { comment ->
                CommentItem(comment = comment)
            }
        }
    }
}

@Composable
fun CommentItem(comment: CommentData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CommonImage(
            data = comment.userImage,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = comment.username ?: "Anonymous",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                comment.timestamp?.let {
                    Text(
                        text = formatTimestamp(it),
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
            Text(
                text = comment.text!!,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}
