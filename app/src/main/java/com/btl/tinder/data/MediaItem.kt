package com.btl.tinder.data

data class MediaItem(
    val url: String = "",
    // "image" or "video"
    val type: String = "image"
) {
    // Add a no-argument constructor for Firestore deserialization
    constructor() : this("", "image")
}
