package com.btl.tinder.data

import coil3.Image

data class UserData(
    var userId: String? = "",
    var name: String? = "",
    var username: String? = "",
    var imageUrl: String? = "",
    var bio: String? = "",
    var gender: String? = "",
    var genderPreference: String? = "",
    var swipesLeft: List<String>? = listOf(),
    var swipesRight: List<String>? = listOf(),
    var matches: List<String>? = listOf(),
    var interests: List<String> = listOf(),
    var address: String? = "",
    var lat: Double? = 0.0,
    var long: Double? = 0.0,
    var ftsComplete: Boolean = false
) {
    fun toMap() = mapOf(
        "userId" to userId,
        "name" to name,
        "username" to username,
        "imageUrl" to imageUrl,
        "bio" to bio,
        "gender" to gender,
        "genderPreference" to genderPreference,
        "swipesLeft" to swipesLeft,
        "swipesRight" to swipesRight,
        "matches" to matches,
        "interests" to interests,
        "address" to address,
        "lat" to lat,
        "long" to long,
        "ftsComplete" to ftsComplete
    )
}

data class ChatData(
    var chatId: String? = "",
    var user1: ChatUser = ChatUser(),
    var user2: ChatUser = ChatUser()
)

data class ChatUser(
    var userId: String? = "",
    var name: String? = "",
    var imageUrl: String? = ""
)


data class CityData(
    val city: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    // Add any other fields you might need, like country
    val country: String? = null
) {
    // Add a no-argument constructor for Firestore deserialization
    constructor() : this(null, null, null, null)
}

data class InterestData(
    val id: String = "",
    val name: String = "",
    val nameNormalized: String = "",
    val category: String = "Other",
    val userGenerated: Boolean = false,
    val approved: Boolean = true,
    val usageCount: Int = 0
) {
    constructor() : this("", "", "", "Other", false, true, 0)
}
