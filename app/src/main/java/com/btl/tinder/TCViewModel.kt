package com.btl.tinder

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.btl.tinder.data.*
import com.btl.tinder.ui.Gender
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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
    //val signedIn = mutableStateOf(false)
    val signInState = mutableStateOf(SignInState.SIGNED_OUT)
    val userData = mutableStateOf<UserData?>(null)

    val matchProfiles = mutableStateOf<List<UserMatch>>(listOf())
    val inProgressProfiles = mutableStateOf(false)

    // --- City Search State ---
    private val _cities = MutableStateFlow<List<CityData>>(emptyList())
    val cities = _cities.asStateFlow()
    private var searchJob: Job? = null

    private val _allInterests = MutableStateFlow<List<InterestData>>(emptyList())
    val allInterests = _allInterests.asStateFlow()
    private val _interestsLoaded = MutableStateFlow(false)
    val interestsLoaded = _interestsLoaded.asStateFlow()

    init {
        val currentUser = auth.currentUser
        // If a user is already logged in, treat it as a normal login flow
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
                                    createOrUpdateProfile(username = username)

                                    // ✅ CONNECT STREAM NGAY SAU KHI SIGNUP
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

                            // ✅ CONNECT STREAM NGAY SAU KHI LOGIN
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

    private fun connectToStream(userId: String, username: String? = null) {
        // Kiểm tra xem đã connect chưa
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
                } else {
                    Log.e("TCViewModel", "❌ Stream connect failed: ${result.errorOrNull()?.message}")

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
        long: Double? = null
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
            address = address, // Use the new address
            lat = lat,         // Use the new latitude
            long = long         // Use the new longitude
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
                    populateCards()

                    // ✅ Connect Stream sau khi có userData (cho trường hợp app restart)
                    if (user != null) {
                        connectToStream(uid)
                    }
                }
            }
    }

    // ---------------------- OTHER LOGIC ----------------------

    fun onLogout() {
        chatClient.disconnect(flushPersistence = true)
        auth.signOut()
        signInState.value = SignInState.SIGNED_OUT
        userData.value = null
        popupNotification.value = Event("Logged out")
    }

    fun updateProfileData(
        name: String,
        username: String,
        bio: String,
        gender: Gender,
        genderPreference: Gender,
        interests: List<String>,
        address: String?,
        lat: Double?,
        long: Double?
    ) {
        createOrUpdateProfile(
            name = name,
            username = username,
            bio = bio,
            gender = gender,
            genderPreference = genderPreference,
            interests = interests,
            address = address,
            lat = lat,
            long = long
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

        // 1. Tính điểm Interest
        val interestScore = calculateJaccardSimilarity(
            currentUser.interests ?: listOf(),
            potential.interests ?: listOf()
        )

        // 2. Tính điểm Distance
        var distanceScore = 0.0

        // ✅ LẤY GIÁ TRỊ AN TOÀN BẰNG CÁCH ÉP KIỂU .toDouble()
        // Dùng safe call (?.) vì lat/long có thể null
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
                1.0 - (distance / MAX_DISTANCE_KM)
            }
        }

        // 3. Tổng điểm
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
                        it.toObject<UserData>()?.let {potential ->
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

        // Kiểm tra xem người kia đã swipe right mình chưa
        val reciprocalMatch = selectedUser.swipesRight?.contains(currentUserId) == true

        if (reciprocalMatch) {
            // ✅ MATCH! Cả 2 đều thích nhau
            popupNotification.value = Event("It's a Match! 💕")

            // 1. Xóa swipesRight của người kia
            db.collection(COLLECTION_USER).document(selectedUserId)
                .update("swipesRight", FieldValue.arrayRemove(currentUserId))

            // 2. Thêm vào matches của cả 2 người
            db.collection(COLLECTION_USER).document(selectedUserId)
                .update("matches", FieldValue.arrayUnion(currentUserId))

            db.collection(COLLECTION_USER).document(currentUserId)
                .update("matches", FieldValue.arrayUnion(selectedUserId))

            // 3. 🔥 TẠO CHANNEL STREAM CHAT thay vì Firebase chat room
            createStreamChatChannel(currentUserId, selectedUserId, selectedUser)

        } else {
            // ❌ Chưa match - chỉ thêm vào swipesRight
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
                        text = "🎉 You matched! Say hi and start chatting!"
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
        // Firestore does not support case-insensitive startsWith, so we query for a range
        // This finds cities where the 'city' field is >= query and < query + '\uf8ff'
        // This effectively works as a prefix search.
        db.collection("cities")
            .orderBy("city")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limit(10) // Limit results to avoid fetching too much data
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

}