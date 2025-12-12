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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.input.ImeAction
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
    val matchedUsers by vm.matchedUsers.collectAsState() // Collect all matched users
    val inProgress = vm.inProgress.value

    LaunchedEffect(Unit) {
        vm.fetchFriendPosts()
        vm.fetchMatchedUsers() // Fetch all matched users
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
            if (inProgress && friendPosts.isEmpty() && matchedUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (matchedUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You haven\'t matched with anyone yet. Keep swiping to see their posts!",
                        color = Color.White,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        fontFamily = playpenFontFamily
                    )
                }
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(matchedUsers, key = { it.userId!! }) { friend ->
                        FriendAvatar(friend = friend)
                    }
                }

                if (friendPosts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You\'ve matched with friends, but they haven\'t posted anything yet!",
                            color = Color.White,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            fontFamily = playpenFontFamily
                        )
                    }
                } else {
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

    val comments by vm.getCommentsFlow(post.userId, post.postId!!).collectAsState(initial = emptyList())
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
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                    ) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Delete post", tint = Color.White)
                        }
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
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val likesCount = post.likes.size
                IconButton(onClick = {
                    currentUser?.userId?.let { userId ->
                        vm.onLikeDislikePost(post, userId)
                    }
                }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color(0xFFFF7898) else Color.Gray
                    )
                }
                if (likesCount > 0) {
                    val likesText = if (likesCount > 1) "$likesCount likes" else "1 like"
                    Text(
                        text = likesText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 4.dp),
                        fontFamily = playpenFontFamily
                    )
                }
            }

            Divider(
                color = Color.Gray.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Always show the comment section
            CommentsSection(
                comments = comments,
                commentInput = commentInput,
                onCommentInputChange = { commentInput = it },
                onCommentSend = {
                    vm.postComment(post.userId, post.postId!!, commentInput)
                    commentInput = ""
                },
                vm = vm,
                post = post
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
            playWhenReady = false
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
    comments: List<CommentData>,
    commentInput: String,
    onCommentInputChange: (String) -> Unit,
    onCommentSend: () -> Unit,
    vm: TCViewModel,
    post: PostData
) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(
            text = "Comments",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp),
            fontFamily = playpenFontFamily
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            androidx.compose.material3.OutlinedTextField(
                value = commentInput,
                onValueChange = onCommentInputChange,
                label = { Text("Add a comment...", color = Color.Gray, fontFamily = playpenFontFamily) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                trailingIcon = {
                    if (commentInput.isNotBlank()) {
                        IconButton(onClick = onCommentSend) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Comment",
                                tint = Color.White
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (commentInput.isNotBlank()) {
                        onCommentSend()
                    }
                })
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (comments.isEmpty()) {
            Text(
                text = "No comments yet.",
                color = Color.Gray,
                fontSize = 14.sp,
                fontFamily = playpenFontFamily
            )
        } else {
            comments.forEach { comment ->
                CommentItem(comment = comment, vm = vm, post = post)
            }
        }
    }
}

@Composable
fun CommentItem(comment: CommentData, vm: TCViewModel, post: PostData) {
    val currentUser = vm.userData.value
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Comment") },
            text = { Text("Are you sure you want to delete this comment?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (comment.commentId != null) {
                            vm.deleteComment(post.userId, post.postId!!, comment.commentId!!)
                        }
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
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = comment.username ?: "Anonymous",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White,
                    fontFamily = playpenFontFamily
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
                text = comment.text.toString(),
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = playpenFontFamily
            )
        }
        if (currentUser?.userId == comment.userId) {
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete comment",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
