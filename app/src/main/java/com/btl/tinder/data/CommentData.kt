package com.btl.tinder.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CommentData(
    val commentId: String? = null,
    val text: String? = null,
    val username: String? = null,
    val userImage: String? = null,
    val userId: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null
)
