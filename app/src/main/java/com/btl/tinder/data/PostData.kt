package com.btl.tinder.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class PostData(
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userImage: String = "",
    val caption: String? = null,
    val media: List<MediaItem> = emptyList(),
    @ServerTimestamp
    val timestamp: Date? = null
) {
    // Add a no-argument constructor for Firestore deserialization
    constructor() : this("", "", "", "", null, emptyList(), null)
}
