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













@HiltViewModel
class TCViewModel12 @Inject constructor(
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

    val matchProfiles = mutableStateOf<List<UserData>>(listOf())
    val inProgressProfiles = mutableStateOf(false)

    // --- City Search State ---
    private val _cities = MutableStateFlow<List<CityData>>(emptyList())
    val cities = _cities.asStateFlow()
    private var searchJob: Job? = null

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

    // ---------------------- MATCHING ----------------------

    private fun populateCards() {
        inProgressProfiles.value = true
        val g = userData.value?.gender?.uppercase() ?: "ANY"
        val gPref = userData.value?.genderPreference?.uppercase() ?: "ANY"

        val cardsQuery = when (Gender.valueOf(gPref)) {
            Gender.MALE -> db.collection(COLLECTION_USER).whereEqualTo("gender", Gender.MALE)
            Gender.FEMALE -> db.collection(COLLECTION_USER).whereEqualTo("gender", Gender.FEMALE)
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
                    handleException(error)
                    return@addSnapshotListener
                }

                val potentials = mutableListOf<UserData>()
                value?.documents?.forEach {
                    it.toObject<UserData>()?.let { potential ->
                        var showUser = true
                        if (
                            userData.value?.swipesLeft?.contains(potential.userId) == true ||
                            userData.value?.swipesRight?.contains(potential.userId) == true ||
                            userData.value?.matches?.contains(potential.userId) == true
                        ) showUser = false
                        if (showUser) potentials.add(potential)
                    }
                }

                matchProfiles.value = potentials
                inProgressProfiles.value = false
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
}
