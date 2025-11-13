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
import java.util.UUID
import javax.inject.Inject

enum class SignInState {
    SIGNED_IN_FROM_LOGIN,
    SIGNED_IN_FROM_SIGNUP,
    SIGNED_OUT
}

@HiltViewModel
class TCViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage,
    private val chatClient: ChatClient
) : ViewModel() {

    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String>?>(null)
    //val signedIn = mutableStateOf(false)
    val signInState = mutableStateOf(SignInState.SIGNED_OUT)
    val userData = mutableStateOf<UserData?>(null)

    val matchProfiles = mutableStateOf<List<UserData>>(listOf())
    val inProgressProfiles = mutableStateOf(false)

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

                                    // ✅ Lấy token từ Cloud Function
                                    getStreamToken { streamToken ->
                                        val user = io.getstream.chat.android.models.User(
                                            id = firebaseUser.uid,
                                            name = username,
                                            image = ""
                                        )

                                        chatClient.connectUser(user, streamToken).enqueue { result ->
                                            if (result.isSuccess) {
                                                Log.d("GetStream", "✅ Connected to Stream after signup!")
                                            } else {
                                                Log.e("GetStream", "❌ Stream connect failed")
                                            }
                                        }
                                    }

                                    signInState.value = SignInState.SIGNED_IN_FROM_SIGNUP
                                    navController.navigate(DestinationScreen.Login.route)
                                }.addOnFailureListener {
                                    handleException(it, "Could not refresh Firebase token")
                                }

                            } else {
                                handleException(task.exception, "Signup failed")
                            }
                            inProgress.value = false
                        }
                } else {
                    inProgress.value = false
                    handleException(customMessage = "Username already exists")
                }
            }
            .addOnFailureListener {
                handleException(it)
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
                            inProgress.value = false
                            getUserData(firebaseUser.uid)

                            getStreamToken { streamToken ->
                                val user = io.getstream.chat.android.models.User(
                                    id = firebaseUser.uid,
                                    name = userData.value?.username ?: "Unknown",
                                    image = userData.value?.imageUrl ?: ""
                                )

                                chatClient.connectUser(user, streamToken).enqueue { result ->
                                    if (result.isSuccess) {
                                        Log.d("GetStream", "✅ Connected to Stream after login!")
                                    } else {
                                        Log.e("GetStream", "❌ Stream connect failed")
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            handleException(it, "Could not refresh Firebase token")
                        }

                } else {
                    handleException(task.exception, "Login failed")
                }
            }
            .addOnFailureListener {
                handleException(it, "Login failed")
            }
    }

    // ---------------------- STREAM TOKEN FIX ----------------------

    fun getStreamToken(onComplete: (String) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            handleException(customMessage = "No Firebase user logged in.")
            return
        }

        // Làm mới token Firebase trước khi gọi function
        user.getIdToken(true)
            .addOnSuccessListener {
                Log.d("GetStreamToken", "✅ Firebase ID token refreshed successfully.")

                // ⚡ Chỉ định region asia-east2
                val functions = FirebaseFunctions.getInstance("asia-east2")

                functions
                    .getHttpsCallable("ext-auth-chat-getStreamUserToken") // Dùng tên function đúng
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
        genderPreference: Gender? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            bio = bio ?: userData.value?.bio,
            gender = gender?.toString() ?: userData.value?.gender,
            genderPreference = genderPreference?.toString() ?: userData.value?.genderPreference
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
        genderPreference: Gender
    ) {
        createOrUpdateProfile(
            name = name,
            username = username,
            bio = bio,
            gender = gender,
            genderPreference = genderPreference
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
        val reciprocalMatch = selectedUser.swipesRight?.contains(userData.value?.userId)
        if (!reciprocalMatch!!) {
            db.collection(COLLECTION_USER).document(userData.value?.userId ?: "")
                .update("swipesRight", FieldValue.arrayUnion(selectedUser.userId))
        } else {
            popupNotification.value = Event("Match!")

            db.collection(COLLECTION_USER).document(selectedUser.userId ?: "")
                .update("swipesRight", FieldValue.arrayRemove(userData.value?.userId))
            db.collection(COLLECTION_USER).document(selectedUser.userId ?: "")
                .update("matches", FieldValue.arrayUnion(userData.value?.userId))
            db.collection(COLLECTION_USER).document(userData.value?.userId ?: "")
                .update("matches", FieldValue.arrayUnion(selectedUser.userId))

            val chatKey = db.collection(COLLECTION_CHAT).document().id
            val chatData = ChatData(
                chatKey,
                ChatUser(
                    userData.value?.userId,
                    userData.value?.name ?: userData.value?.username,
                    userData.value?.imageUrl
                ),
                ChatUser(
                    selectedUser.userId,
                    selectedUser.name ?: selectedUser.username,
                    selectedUser.imageUrl
                )
            )
            db.collection(COLLECTION_CHAT).document(chatKey).set(chatData)
        }
    }
}
