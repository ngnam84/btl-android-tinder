package com.btl.tinder

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.btl.tinder.data.*
import com.btl.tinder.ui.Gender
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.PushProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.joinAll
import java.util.Date

enum class SignInState {
    SIGNED_IN_FROM_LOGIN,
    SIGNED_IN_FROM_SIGNUP,
    SIGNED_OUT
}

data class UserMatch(
    val user: UserData,
    val score: Double
)

@HiltViewModel
class TCViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage,
    val chatClient: ChatClient
) : ViewModel() {

    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String>?>(null)
    val signInState = mutableStateOf(SignInState.SIGNED_OUT)
    val userData = mutableStateOf<UserData?>(null)
    val posts = mutableStateOf<List<PostData>>(listOf())
    val profileDetailPosts = mutableStateOf<List<PostData>>(listOf())
    val friendPosts = mutableStateOf<List<PostData>>(listOf())
    val inProgressComments = mutableStateOf(false)
    // Removed global _comments and comments, now handled per-post via getCommentsFlow

    val matchProfiles = mutableStateOf<List<UserMatch>>(listOf())
    val inProgressProfiles = mutableStateOf(false)

    private val _cities = MutableStateFlow<List<CityData>>(emptyList())
    val cities = _cities.asStateFlow()
    private var searchJob: Job? = null

    private val _allInterests = MutableStateFlow<List<InterestData>>(emptyList())
    val allInterests = _allInterests.asStateFlow()
    private val _interestsLoaded = MutableStateFlow(false)
    val interestsLoaded = _interestsLoaded.asStateFlow()

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    init {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            signInState.value = SignInState.SIGNED_IN_FROM_LOGIN
            currentUser.uid.let { uid ->
                getUserData(uid)
            }
        } else {
            signInState.value = SignInState.SIGNED_OUT
        }
    }

    // ---------------------- AUTH & USER ----------------------

    fun onSignup(username: String, email: String, pass: String, navController: NavController) {
        if (username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }

        inProgress.value = true
        db.collection(COLLECTION_USER).whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser = auth.currentUser
                                if (firebaseUser == null) {
                                    handleException(customMessage = "Firebase user is null after signup")
                                    return@addOnCompleteListener
                                }

                                firebaseUser.getIdToken(true).addOnSuccessListener {
                                    createOrUpdateProfile(username = username, ftsComplete = false)
                                    connectToStream(firebaseUser.uid, username)
                                    signInState.value = SignInState.SIGNED_IN_FROM_SIGNUP
                                    navController.navigate(DestinationScreen.Login.route)
                                }.addOnFailureListener {
                                    handleException(it, "Could not refresh Firebase token")
                                }
                                inProgress.value = false
                            } else {
                                handleException(task.exception, "Signup failed")
                                inProgress.value = false
                            }
                        }
                } else {
                    handleException(customMessage = "Username already exists")
                    inProgress.value = false
                }
            }
            .addOnFailureListener {
                handleException(it)
                inProgress.value = false
            }
    }

    fun onLogin(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }

        inProgress.value = true
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser == null) {
                        handleException(customMessage = "Firebase user is null after login")
                        return@addOnCompleteListener
                    }

                    firebaseUser.getIdToken(true)
                        .addOnSuccessListener {
                            signInState.value = SignInState.SIGNED_IN_FROM_LOGIN
                            getUserData(firebaseUser.uid)
                            connectToStream(firebaseUser.uid)
                            inProgress.value = false
                        }
                        .addOnFailureListener {
                            handleException(it, "Could not refresh Firebase token")
                            inProgress.value = false
                        }
                } else {
                    handleException(task.exception, "Login failed")
                    inProgress.value = false
                }
            }
            .addOnFailureListener {
                handleException(it, "Login failed")
                inProgress.value = false
            }
    }
    fun changePassword(currentPassword: String, newPassword: String, confirmNewPassword: String) {
        if (newPassword != confirmNewPassword) {
            handleException(customMessage = "New passwords do not match")
            return
        }

        if (newPassword.length < 6) {
            handleException(customMessage = "New password must be at least 6 characters")
            return
        }

        inProgress.value = true
        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    popupNotification.value = Event("Password updated successfully")
                                    inProgress.value = false
                                } else {
                                    handleException(updateTask.exception, "Failed to update password")
                                }
                            }
                    } else {
                        handleException(reauthTask.exception, "Re-authentication failed")
                    }
                }
        } else {
            handleException(customMessage = "User not found")
        }
    }

    fun deleteAccount(password: String, onAccountDeleted: () -> Unit) {
        inProgress.value = true
        val user = auth.currentUser
        if (user == null || user.email == null) {
            handleException(customMessage = "User not logged in or email is missing.")
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, password)

        // Re-authenticate user before deleting
        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                viewModelScope.launch {
                    try {
                        val uid = user.uid
                        val userDoc = userData.value

                        // 1. Delete all media from Storage
                        val storageDeletions = mutableListOf<Job>()

                        // Delete profile picture
                        userDoc?.imageUrl?.let {
                            if (it.isNotBlank()) {
                                storageDeletions.add(launch { storage.getReferenceFromUrl(it).delete().await() })
                            }
                        }
                        // Delete all post media
                        posts.value.forEach { post ->
                            post.media.forEach { media ->
                                storageDeletions.add(launch { storage.getReferenceFromUrl(media.url).delete().await() })
                            }
                        }
                        storageDeletions.joinAll()

                        // 2. Delete all posts sub-collection documents
                        val postsCollection = db.collection(COLLECTION_USER).document(uid).collection("posts")
                        val allPosts = postsCollection.get().await()
                        for (postDoc in allPosts.documents) {
                            postsCollection.document(postDoc.id).delete().await()
                        }

                        // 3. Delete user document from Firestore
                        db.collection(COLLECTION_USER).document(uid).delete().await()

                        // 4. Delete user from Auth
                        user.delete().await()

                        popupNotification.value = Event("Account deleted successfully.")
                        onLogout() // Disconnects from Stream, signs out, and clears local data
                        onAccountDeleted()

                    } catch (e: Exception) {
                        handleException(e, "Failed to delete account.")
                    } finally {
                        inProgress.value = false
                    }
                }
            } else {
                handleException(reauthTask.exception, "Re-authentication failed. Please check your password.")
            }
        }
    }

    private fun connectToStream(userId: String, username: String? = null) {
        val currentUser = chatClient.clientState.user.value
        if (currentUser != null && currentUser.id == userId) {
            Log.d("TCViewModel", "✅ Already connected to Stream")
            return
        }

        Log.d("TCViewModel", "🔄 Connecting to Stream for user: $userId")

        getStreamToken { streamToken ->
            val user = io.getstream.chat.android.models.User(
                id = userId,
                name = username ?: userData.value?.name ?: userData.value?.username ?: "Unknown",
                image = userData.value?.imageUrl ?: ""
            )

            chatClient.connectUser(user, streamToken).enqueue { result ->
                if (result.isSuccess) {
                    Log.d("TCViewModel", "✅ Connected to Stream successfully!")
                    registerFCMToken()
                } else {
                    Log.e("TCViewModel", "❌ Stream connect failed: ${result.errorOrNull()?.message}")
                }
            }
        }
    }

    private fun registerFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("TCViewModel", "❌ Failed to get FCM token", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result ?: ""
            if (token.isEmpty()) {
                Log.e("TCViewModel", "❌ FCM token is empty")
                return@addOnCompleteListener
            }

            Log.d("TCViewModel", "✅ FCM Token: $token")

            val currentUser = chatClient.clientState.user.value
            if (currentUser == null) {
                Log.w("TCViewModel", "User not connected yet, token will be registered after connection")
                return@addOnCompleteListener
            }

            val device = io.getstream.chat.android.models.Device(
                token = token,
                pushProvider = io.getstream.chat.android.models.PushProvider.FIREBASE,
                providerName = "firebase_push"
            )

            chatClient.addDevice(device).enqueue { result ->
                if (result.isSuccess) {
                    Log.d("TCViewModel", "✅ FCM token registered with Stream")
                } else {
                    Log.e("TCViewModel", "❌ Failed to register FCM token: ${result.errorOrNull()?.message}")
                }
            }
        }
    }

    // ---------------------- STREAM TOKEN FIX ----------------------

    fun getStreamToken(onComplete: (String) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            handleException(customMessage = "No Firebase user logged in.")
            return
        }

        user.getIdToken(true)
            .addOnSuccessListener {
                Log.d("GetStreamToken", "✅ Firebase ID token refreshed successfully.")

                val functions = FirebaseFunctions.getInstance("asia-east2")

                functions
                    .getHttpsCallable("ext-auth-chat-getStreamUserToken")
                    .call()
                    .addOnSuccessListener { result ->
                        val token = result.data as? String
                        if (token != null) {
                            Log.d("GetStreamToken", "✅ Received Stream token successfully.")
                            onComplete(token)
                        } else {
                            Log.e("GetStreamToken", "❌ Token returned is null or invalid.")
                            handleException(customMessage = "Invalid Stream token response.")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("GetStreamToken", "❌ Error calling function: ${e.message}", e)
                        handleException(e, "Error calling GetStream token function.")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("GetStreamToken", "❌ Failed to refresh Firebase token: ${e.message}", e)
                handleException(e, "Failed to refresh Firebase token.")
            }
    }

    // ---------------------- USER DATA & PROFILE ----------------------

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null,
        gender: Gender? = null,
        genderPreference: Gender? = null,
        interests: List<String>? = null,
        address: String? = null,
        lat: Double? = null,
        long: Double? = null,
        ftsComplete: Boolean? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            bio = bio ?: userData.value?.bio,
            gender = gender?.toString() ?: userData.value?.gender,
            genderPreference = genderPreference?.toString() ?: userData.value?.genderPreference,
            interests = interests ?: userData.value?.interests ?: listOf(),
            address = address,
            lat = lat,
            long = long,
            ftsComplete = ftsComplete ?: userData.value?.ftsComplete ?: false
        )

        uid?.let {
            inProgress.value = true
            db.collection(COLLECTION_USER).document(uid)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener {
                                this.userData.value = userData
                                inProgress.value = false
                                popupNotification.value = Event("Profile updated")
                            }
                            .addOnFailureListener { it ->
                                handleException(it, "Cannot update user")
                                inProgress.value = false
                            }
                    } else {
                        db.collection(COLLECTION_USER).document(uid).set(userData)
                        inProgress.value = false
                        getUserData(uid)
                    }
                }
                .addOnFailureListener {
                    handleException(it, "Cannot create user")
                    inProgress.value = false
                }
        }
    }

    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(COLLECTION_USER).document(uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("TCViewModel", "Error getting user data: ${error.message}")
                    handleException(error, "Cannot get user data")
                }
                if (value != null) {
                    val user = value.toObject<UserData>()
                    userData.value = user
                    inProgress.value = false
                    Log.d("TCViewModel", "User data loaded: ${userData.value}")
                    getPosts(uid)
                    populateCards()

                    if (user != null) {
                        connectToStream(uid)
                    }
                }
            }
    }

    // ---------------------- LOGOUT ----------------------

    fun onLogout() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                chatClient.getDevices().enqueue { devicesResult ->
                    if (devicesResult.isSuccess) {
                        val devices = devicesResult.getOrNull()
                        devices?.forEach { device ->
                            chatClient.deleteDevice(device).enqueue { result ->
                                if (result.isSuccess) {
                                    Log.d("TCViewModel", "✅ Device token removed")
                                } else {
                                    Log.e("TCViewModel", "❌ Failed to remove device: ${result.errorOrNull()?.message}")
                                }
                            }
                        }
                    }
                }
            }
        }

        chatClient.disconnect(flushPersistence = true).enqueue()
        auth.signOut()
        signInState.value = SignInState.SIGNED_OUT
        userData.value = null
        popupNotification.value = Event("Logged out")
    }

    // ---------------------- OTHER LOGIC ----------------------

    suspend fun updateProfileData(
        name: String,
        username: String,
        bio: String,
        gender: Gender,
        genderPreference: Gender,
        interests: List<String>,
        address: String?,
        lat: Double?,
        long: Double?,
        ftsComplete: Boolean
    ) {
        var finalLat = lat
        var finalLong = long
        var finalAddress = address

        if (finalLat == null && finalLong == null && !address.isNullOrBlank()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val geoCodedCoords = getLatLongFromAddress(address)
                if (geoCodedCoords != null) {
                    finalLat = geoCodedCoords.first
                    finalLong = geoCodedCoords.second
                } else {
                    Log.e("TCViewModel", "Geocoding failed for address: $address")
                    popupNotification.value = Event("Could not find location for the entered city. Please try again with a more specific address or select from suggestions.")
                    finalAddress = null
                }
            } else {
                Log.w("TCViewModel", "Geoapify API call requires Android O (API 26) or higher.")
                popupNotification.value = Event("Location services require a newer Android version.")
                finalAddress = null
            }
        }

        createOrUpdateProfile(
            name = name,
            username = username,
            bio = bio,
            gender = gender,
            genderPreference = genderPreference,
            interests = interests,
            address = finalAddress,
            lat = finalLat,
            long = finalLong,
            ftsComplete = ftsComplete
        )
    }

    private fun uploadImage(uri: Uri, onImageUploaded: (Uri) -> Unit) {
        inProgress.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)
        uploadTask
            .addOnSuccessListener {
                val res = it.metadata?.reference?.downloadUrl
                res?.addOnSuccessListener(onImageUploaded)
            }
            .addOnFailureListener {
                handleException(it)
                inProgress.value = false
            }
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(imageUrl = it.toString())
        }
    }

    private fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("LoveMatch", "Exception", exception)
        val message = customMessage.ifEmpty { exception?.localizedMessage ?: "Unknown error" }
        popupNotification.value = Event(message)
        inProgress.value = false
    }

    private fun calculateJaccardSimilarity(
        interests1: List<String>,
        interests2: List<String>
    ): Double {
        if (interests1.isEmpty() && interests2.isEmpty()) return 0.0
        if (interests1.isEmpty() || interests2.isEmpty()) return 0.0

        val intersection = interests1.intersect(interests2.toSet())
        val union = interests1.union(interests2.toSet())

        return intersection.size.toDouble() / union.size.toDouble()
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    private fun calculateMatchScore(
        currentUser: UserData,
        potential: UserData
    ): Double {
        val INTEREST_WEIGHT = 0.7
        val DISTANCE_WEIGHT = 0.3
        val MAX_DISTANCE_KM = 100.0

        val interestScore = calculateJaccardSimilarity(
            currentUser.interests ?: listOf(),
            potential.interests ?: listOf()
        )

        var distanceScore = 0.0

        val lat1 = currentUser.lat?.toDouble()
        val lon1 = currentUser.long?.toDouble()
        val lat2 = potential.lat?.toDouble()
        val lon2 = potential.long?.toDouble()

        if (lat1 != null && lon1 != null && lat2 != null && lon2 != null) {
            val distance = calculateDistance(lat1, lon1, lat2, lon2)
            Log.d("MatchCalc", "Khoảng cách tới ${potential.name}: $distance km")
            distanceScore = if (distance > MAX_DISTANCE_KM) {
                0.0
            } else {
                1.0 - (distance / MAX_DISTANCE_KM) - 0.5
            }
        }

        return (INTEREST_WEIGHT * interestScore) + (DISTANCE_WEIGHT * distanceScore)
    }

    // ---------------------- MATCHING ----------------------

    private fun populateCards() {
        inProgressProfiles.value = true
        Log.d("TCViewModel", "populateCards called. Current User Data: ${userData.value}")

        val g = if (userData.value?.gender.isNullOrEmpty()) "ANY"
        else userData.value!!.gender!!.uppercase()
        val gPref = if (userData.value?.genderPreference.isNullOrEmpty()) "ANY"
        else userData.value!!.genderPreference!!.uppercase()

        Log.d("TCViewModel", "User Gender: $g, Preference: $gPref")

        val cardsQuery =
            when (Gender.valueOf(gPref)) {
                Gender.MALE -> db.collection(COLLECTION_USER)
                    .whereEqualTo("gender", Gender.MALE)
                Gender.FEMALE -> db.collection(COLLECTION_USER)
                    .whereEqualTo("gender", Gender.FEMALE)
                Gender.ANY -> db.collection(COLLECTION_USER)
            }

        val userGender = Gender.valueOf(g)

        cardsQuery.where(
            com.google.firebase.firestore.Filter.and(
                com.google.firebase.firestore.Filter.notEqualTo("userId", userData.value?.userId),
                com.google.firebase.firestore.Filter.or(
                    com.google.firebase.firestore.Filter.equalTo("genderPreference", userGender),
                    com.google.firebase.firestore.Filter.equalTo("genderPreference", Gender.ANY)
                )
            )
        )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    inProgressProfiles.value = false
                    Log.e("TCViewModel", "Error fetching cards: ${error.message}", error)
                    handleException(error)
                }
                if (value != null) {
                    Log.d("TCViewModel", "Fetched ${value.documents.size} documents from Firestore.")
                    val potentials = mutableListOf<UserData>()
                    value.documents.forEach {
                        it.toObject<UserData>()?.let { potential ->
                            var showUser = true
                            Log.d("TCViewModel", "Processing potential user: ${potential.userId}, Name: ${potential.name}")
                            if (
                                userData.value?.swipesLeft?.contains(potential.userId) == true ||
                                userData.value?.swipesRight?.contains(potential.userId) == true ||
                                userData.value?.matches?.contains(potential.userId) == true
                            ) {
                                showUser = false
                                Log.d("TCViewModel", "User ${potential.userId} already swiped/matched. Not showing.")
                            }
                            if (showUser)
                                potentials.add(potential)
                        }
                    }

                    Log.d("TCViewModel", "Found ${potentials.size} potential matches after filtering.")
                    Log.d("TCViewModel", "===== CALCULATING SCORES =====")

                    val rankedMatches = potentials
                        .map { UserMatch(it, calculateMatchScore(userData.value!!, it)) }
                        .filter { it.score >= 0.0 }
                        .sortedBy { it.score }

                    Log.d("TCViewModel", "Ranked matches: ${rankedMatches.size}")
                    matchProfiles.value = rankedMatches
                    inProgressProfiles.value = false
                }
            }
    }

    fun onDislike(selectedUser: UserData) {
        db.collection(COLLECTION_USER).document(userData.value?.userId ?: "")
            .update("swipesLeft", FieldValue.arrayUnion(selectedUser.userId))
    }

    fun onLike(selectedUser: UserData) {
        val currentUserId = userData.value?.userId ?: ""
        val selectedUserId = selectedUser.userId ?: ""

        if (currentUserId.isEmpty() || selectedUserId.isEmpty()) {
            handleException(customMessage = "Invalid user data")
            return
        }

        val reciprocalMatch = selectedUser.swipesRight?.contains(currentUserId) == true

        if (reciprocalMatch) {
            popupNotification.value = Event("It's a Match! 💕")

            db.collection(COLLECTION_USER).document(selectedUserId)
                .update("swipesRight", FieldValue.arrayRemove(currentUserId))

            db.collection(COLLECTION_USER).document(selectedUserId)
                .update("matches", FieldValue.arrayUnion(currentUserId))

            db.collection(COLLECTION_USER).document(currentUserId)
                .update("matches", FieldValue.arrayUnion(selectedUserId))

            createStreamChatChannel(currentUserId, selectedUserId, selectedUser)

        } else {
            db.collection(COLLECTION_USER).document(currentUserId)
                .update("swipesRight", FieldValue.arrayUnion(selectedUserId))
                .addOnSuccessListener {
                    Log.d("TCViewModel", "Swiped right on: ${selectedUser.name}")
                }
                .addOnFailureListener {
                    handleException(it, "Failed to update swipes")
                }
        }
    }

    private fun createStreamChatChannel(
        currentUserId: String,
        matchedUserId: String,
        matchedUser: UserData
    ) {
        val connectionState = chatClient.clientState.connectionState.value
        Log.d("TCViewModel", "Stream connection state: $connectionState")

        if (connectionState != io.getstream.chat.android.models.ConnectionState.Connected) {
            Log.e("TCViewModel", "❌ Stream not connected. State: $connectionState")
            handleException(customMessage = "Chat not connected. Please try again.")
            return
        }

        val channelId = listOf(currentUserId, matchedUserId).sorted().joinToString("-")

        Log.d("TCViewModel", "Creating channel with ID: $channelId")
        Log.d("TCViewModel", "Members: $currentUserId, $matchedUserId")

        val channel = chatClient.channel(
            channelType = "messaging",
            channelId = channelId
        )

        channel.create(
            memberIds = listOf(currentUserId, matchedUserId),
            extraData = mapOf(
                "created_by_match" to true
            )
        ).enqueue { result ->
            if (result.isSuccess) {
                Log.d("TCViewModel", "✅ Stream channel created successfully: $channelId")

                channel.sendMessage(
                    message = io.getstream.chat.android.models.Message(
                        text = "Hey ${matchedUser.name}! We matched! 💕 I'd love to chat and get to know you better! 😊"
                    )
                ).enqueue { msgResult ->
                    if (msgResult.isSuccess) {
                        Log.d("TCViewModel", "✅ Welcome message sent")
                    } else {
                        Log.e("TCViewModel", "❌ Failed to send message: ${msgResult.errorOrNull()?.message}")
                    }
                }

            } else {
                val error = result.errorOrNull()
                Log.e("TCViewModel", "❌ Failed to create channel")
                Log.e("TCViewModel", "Error message: ${error?.message}")
                Log.e("TCViewModel", "Error details: $error")
                handleException(customMessage = "Could not create chat: ${error?.message}")
            }
        }
    }

    fun searchCities(query: String) {
        if (query.length < 2) {
            _cities.value = emptyList()
            return
        }
        db.collection("cities")
            .orderBy("city")
            .startAt(query)
            .endAt(query + "")
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                _cities.value = documents.mapNotNull { it.toObject<CityData>() }
            }
            .addOnFailureListener { exception ->
                Log.w("TCViewModel", "Error getting cities: ", exception)
                _cities.value = emptyList()
            }
    }

    fun loadAllInterests() {
        db.collection("interests")
            .whereEqualTo("approved", true)
            .orderBy("usageCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val interests = snapshot.documents.mapNotNull {
                    it.toObject<InterestData>()
                }
                _allInterests.value = interests
                _interestsLoaded.value = true
                Log.d("TCViewModel", "Loaded ${interests.size} interests")
            }
            .addOnFailureListener { e ->
                Log.e("TCViewModel", "Error loading interests", e)
                _interestsLoaded.value = true
            }
    }

    fun incrementInterestUsage(interestId: String) {
        db.collection("interests").document(interestId)
            .update("usageCount", FieldValue.increment(1))
    }

    fun addNewInterest(name: String, onComplete: (InterestData) -> Unit) {
        val newInterest = InterestData(
            id = db.collection("interests").document().id,
            name = name,
            nameNormalized = name.lowercase(),
            category = "User Generated",
            userGenerated = true,
            approved = false,
            usageCount = 1
        )

        db.collection("interests").document(newInterest.id)
            .set(newInterest)
            .addOnSuccessListener {
                Log.d("TCViewModel", "New interest added: ${newInterest.name}")
                onComplete(newInterest)
            }
            .addOnFailureListener { e ->
                Log.e("TCViewModel", "Error adding interest", e)
                handleException(e, "Could not add interest")
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getLatLongFromAddress(address: String): Pair<Double, Double>? {
        val apiKey = "eedd1bc6f483429793b03110c4f4e9ce"
        val url = "https://api.geoapify.com/v1/geocode/search?text=$address&apiKey=$apiKey"

        return try {
            val response = httpClient.get(url)
            if (response.status.value == 200) {
                val geoapifyResponse = response.body<GeoapifyResponse>()
                val feature = geoapifyResponse.features.firstOrNull()
                if (feature != null) {
                    Pair(feature.properties.lat, feature.properties.lon)
                } else {
                    Log.e("Geoapify", "No features found for address: $address")
                    null
                }
            } else {
                Log.e("Geoapify", "API call failed with status: ${response.status.value}")
                Log.e("Geoapify", "Response body: ${response.bodyAsText()}")
                null
            }
        } catch (e: Exception) {
            Log.e("Geoapify", "Error getting lat/long from Geoapify: ${e.message}", e)
            handleException(e, "Error getting location data")
            null
        }
    }


    // ---------------------- POSTS ----------------------

    fun createPost(caption: String, localMedia: List<LocalMediaItem>, onPostCreated: () -> Unit) {
        viewModelScope.launch {
            inProgress.value = true
            val currentUser = userData.value

            try {
                if (currentUser?.userId == null) {
                    handleException(customMessage = "User not logged in")
                    return@launch
                }

                val uploadedMediaItems = mutableListOf<com.btl.tinder.data.MediaItem>()
                for (item in localMedia) {
                    val folder = if (item.type == "image") "images" else "videos"
                    val storageRef = storage.reference.child("posts/${currentUser.userId}/$folder/${UUID.randomUUID()}")
                    val downloadUrl = storageRef.putFile(item.uri).await().storage.downloadUrl.await().toString()

                    uploadedMediaItems.add(com.btl.tinder.data.MediaItem(url = downloadUrl, type = item.type))
                    Log.d("TCViewModel", "Uploaded ${item.type} to: $downloadUrl")
                }

                val postId = db.collection(COLLECTION_USER).document().id
                val post = PostData(
                    postId = postId,
                    userId = currentUser.userId!!,
                    username = currentUser.username ?: currentUser.name ?: "",
                    userImage = currentUser.imageUrl ?: "",
                    caption = caption.takeIf { it.isNotBlank() },
                    media = uploadedMediaItems
                )

                db.collection(COLLECTION_USER).document(currentUser.userId!!)
                    .collection("posts").document(postId).set(post)
                    .await()

                popupNotification.value = Event("Post created successfully!")
                onPostCreated()

            } catch (e: Exception) {
                handleException(e, "Failed to create post.")
            } finally {
                inProgress.value = false
            }
        }
    }

    fun deletePost(post: PostData) {
        viewModelScope.launch {
            inProgress.value = true
            try {
                // 1. Delete all media from Firebase Storage in parallel
                val deleteJobs = post.media.map {
                    async {
                        try {
                            storage.getReferenceFromUrl(it.url).delete().await()
                            Log.d("TCViewModel", "Deleted from storage: ${it.url}")
                        } catch (e: Exception) {
                            Log.e("TCViewModel", "Failed to delete media from storage: ${it.url}", e)
                        }
                    }
                }
                deleteJobs.awaitAll()

                // 2. Delete the post document from Firestore
                db.collection(COLLECTION_USER).document(post.userId)
                    .collection("posts").document(post.postId)
                    .delete()
                    .await()

                popupNotification.value = Event("Post deleted successfully")

            } catch (e: Exception) {
                handleException(e, "Failed to delete post.")
            } finally {
                inProgress.value = false
            }
        }
    }

    fun getPosts(uid: String) {
        db.collection(COLLECTION_USER).document(uid)
            .collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error, "Failed to get posts")
                    return@addSnapshotListener
                }
                if (value != null) {
                    posts.value = value.documents.mapNotNull { it.toObject<PostData>() }
                }
            }
    }

    fun getPostsForUser(userId: String) {
        db.collection(COLLECTION_USER).document(userId)
            .collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error, "Failed to get posts for user $userId")
                    return@addSnapshotListener
                }
                if (value != null) {
                    profileDetailPosts.value = value.documents.mapNotNull { it.toObject<PostData>() }
                }
            }
    }

    fun fetchFriendPosts() {
        viewModelScope.launch {
            inProgress.value = true
            try {
                val currentUser = userData.value
                if (currentUser?.matches.isNullOrEmpty()) {
                    friendPosts.value = emptyList()
                    return@launch
                }

                val friendIds = currentUser.matches
                val allPosts = mutableListOf<PostData>()

                val friendPostsDeferred = friendIds?.map { friendId ->
                    async {
                        db.collection(COLLECTION_USER).document(friendId)
                            .collection("posts")
                            .get()
                            .await()
                            .documents.mapNotNull { it.toObject<PostData>() }
                    }
                }

                allPosts.addAll(friendPostsDeferred!!.awaitAll().flatten())

                friendPosts.value = allPosts.sortedByDescending { it.timestamp }

            } catch (e: Exception) {
                handleException(e, "Failed to fetch friend posts.")
            } finally {
                inProgress.value = false
            }
        }
    }

    fun onLikeDislikePost(post: PostData, currentUserId: String) {
        viewModelScope.launch {
            val postRef = db.collection(COLLECTION_USER).document(post.userId).collection("posts").document(post.postId)

            if (post.likes.contains(currentUserId)) {
                postRef.update("likes", FieldValue.arrayRemove(currentUserId)).await()
            } else {
                postRef.update("likes", FieldValue.arrayUnion(currentUserId)).await()
            }
            // Refresh the posts to get the updated like count
            fetchFriendPosts()
        }
    }

    fun postComment(authorId: String, postId: String, text: String) {
        val currentUser = userData.value ?: return
        val comment = CommentData(
            text = text,
            username = currentUser.username ?: currentUser.name,
            userImage = currentUser.imageUrl,
            userId = currentUser.userId,
            timestamp = Date()
        )
        db.collection(COLLECTION_USER).document(authorId).collection("posts").document(postId).collection("comments").add(comment)
            .addOnFailureListener { e ->
                handleException(e, "Failed to post comment.")
            }
    }

    fun getCommentsFlow(authorId: String, postId: String): Flow<List<CommentData>> = callbackFlow {
        inProgressComments.value = true
        val subscription = db.collection(COLLECTION_USER).document(authorId).collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleException(error, "Failed to fetch comments.")
                    inProgressComments.value = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { it.toObject<CommentData>() }
                    trySend(comments)
                }
                inProgressComments.value = false
            }
        awaitClose { subscription.remove() }
    }
}
