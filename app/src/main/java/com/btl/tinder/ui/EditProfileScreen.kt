package com.btl.tinder.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.Icon
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedButton
import androidx.navigation.NavController
import androidx.compose.material3.LinearProgressIndicator
import com.btl.tinder.CommonProgressSpinner
import com.btl.tinder.TCViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.TextField
import com.btl.tinder.CommonDivider
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.btl.tinder.DestinationScreen
import com.btl.tinder.navigateTo
import androidx.compose.runtime.setValue
import com.btl.tinder.CommonImage
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import meshGradient
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.collectAsState
import com.btl.tinder.data.InterestData
import androidx.compose.foundation.layout.Spacer
import com.btl.tinder.FinalInterestValidator
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Close
import com.btl.tinder.ui.theme.deliusFontFamily
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import com.btl.tinder.data.CityData
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.SideEffect
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.foundation.border
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.TextStyle

enum class Gender {
    MALE, FEMALE, ANY
}

@Composable
fun EditProfileScreen(navController: NavController, vm: TCViewModel) {
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = true
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent
        )
    }

    val inProgress = vm.inProgress.value
    val scope = rememberCoroutineScope()

    if (inProgress)
        CommonProgressSpinner()
    else{
        val userData = vm.userData.value
        val g = if(userData?.gender.isNullOrEmpty()) "MALE"
        else userData.gender!!.uppercase()
        val gPrefer = if(userData?.genderPreference.isNullOrEmpty()) "FEMALE"
        else userData.genderPreference!!.uppercase()

        var name by rememberSaveable { mutableStateOf(userData?.name ?: "") }
        var username by rememberSaveable { mutableStateOf(userData?.username ?: "") }
        var bio by rememberSaveable { mutableStateOf(userData?.bio ?: "") }
        var gender by rememberSaveable { mutableStateOf(Gender.valueOf(g)) }
        var genderPreference by rememberSaveable { mutableStateOf(Gender.valueOf(gPrefer)) }
        var interests by rememberSaveable { mutableStateOf(userData?.interests ?: listOf())}

        // State for address, adapted from FTSProfileScreen
        var selectedCity by remember { mutableStateOf<CityData?>(null) }
        var cityInput by rememberSaveable { mutableStateOf(userData?.address ?: "") }


        Box(modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White,
                        Color.White,
                        Color(0xFFFFC1CC),
                        Color(0xFFD1C4E9),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 2500f)
                )
            )
        ){
            Column(modifier = Modifier
                .statusBarsPadding()
                .fillMaxSize()
            ){
                ProfileContent(
                    modifier = Modifier
                        .weight(1f),
                    vm=vm,
                    navController = navController,
                    name=name,
                    username=username,
                    bio = bio,
                    gender = gender,
                    genderPreference = genderPreference,
                    interests = interests,
                    cityInput = cityInput,
                    onNameChange = {name = it},
                    onUsernameChange = {username = it},
                    onBioChange = { bio = it},
                    onGenderChange = {gender = it},
                    onGenderPreferenceChange = {genderPreference=it},
                    onInterestsChange = {interests = it},
                    onCityInputChanged = { cityInput = it },
                    onCitySelected = {
                        selectedCity = it
                        cityInput = it?.city ?: ""
                    },
                    onSave = {
                        scope.launch {
                            val addressToSave = selectedCity?.city ?: cityInput.ifBlank { null }
                            val latToSave = selectedCity?.lat
                            val longToSave = selectedCity?.lng

                            vm.updateProfileData(
                                name = name,
                                username = username,
                                bio = bio,
                                gender = gender,
                                genderPreference = genderPreference,
                                interests = interests,
                                address = addressToSave,
                                lat = latToSave,
                                long = longToSave,
                                ftsComplete = true
                            )
                        }
                    },
                    onBack = { navigateTo(navController, DestinationScreen.Profile.route) },
                    onLogout = {
                        vm.onLogout()
                        navigateTo(navController, DestinationScreen.Login.route)
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    modifier: Modifier,
    vm: TCViewModel,
    navController: NavController,
    name: String,
    username: String,
    bio: String,
    gender: Gender,
    genderPreference: Gender,
    interests: List<String>,
    cityInput: String,
    onNameChange:(String) -> Unit,
    onUsernameChange:(String) -> Unit,
    onBioChange:(String) -> Unit,
    onGenderChange:(Gender) -> Unit,
    onGenderPreferenceChange:(Gender) -> Unit,
    onInterestsChange:(List<String>) -> Unit,
    onCityInputChanged: (String) -> Unit,
    onCitySelected: (CityData?) -> Unit,
    onSave: () -> Unit,
    onBack :() -> Unit,
    onLogout : () -> Unit

){
    val imageUrl = vm.userData.value?.imageUrl
    val scrollState = rememberScrollState()
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var passwordForDelete by remember { mutableStateOf("") }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = { confirmNewPassword = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.changePassword(currentPassword, newPassword, confirmNewPassword)
                    showChangePasswordDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account?", color = Color.Red) },
            text = {
                Column {
                    Text("This is permanent and cannot be undone. All your data, including matches and chats, will be deleted. Please enter your password to confirm.")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = passwordForDelete,
                        onValueChange = { passwordForDelete = it },
                        label = { Text("Enter your password") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                    vm.deleteAccount(passwordForDelete) {
                        navigateTo(navController, DestinationScreen.Login.route)
                    }
                    showDeleteAccountDialog = false
                 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("DELETE", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


    Column(modifier = modifier
        .verticalScroll(scrollState)
        .navigationBarsPadding()
        .imePadding()
        .padding(horizontal = 16.dp)
    ){
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text("Back",Modifier.clickable {onBack.invoke()},
                fontFamily = deliusFontFamily,
                color = Color(0xFFFF789B),
                fontWeight = FontWeight.Bold)
            Text("Save",Modifier.clickable {onSave.invoke()},
                fontFamily = deliusFontFamily,
                color = Color(0xFFFF789B),
                fontWeight = FontWeight.Bold)
        }

        ProfileImage(imageUrl = imageUrl,vm = vm)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Username", fontFamily = deliusFontFamily, color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontFamily = deliusFontFamily, color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                cursorColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name", fontFamily = deliusFontFamily, color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontFamily = deliusFontFamily, color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                cursorColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = bio,
            onValueChange = onBioChange,
            label = { Text("Bio", fontFamily = deliusFontFamily, color = Color.Black) },
            singleLine = false,
            modifier = Modifier.fillMaxWidth().height(120.dp),
            textStyle = TextStyle(fontFamily = deliusFontFamily, color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                cursorColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(Modifier.height(16.dp))
        CommonDivider()
        Spacer(Modifier.height(8.dp))

        Column(Modifier.fillMaxWidth()) {
            Text("I am a:", fontFamily = deliusFontFamily, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically){
                    RadioButton(
                        selected = gender == Gender.MALE,
                        onClick = {onGenderChange(Gender.MALE)},
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFFFF789B),
                            unselectedColor = Color.Black
                        )
                    )
                    Text(
                        text = "Male",
                        modifier = Modifier.clickable { onGenderChange(Gender.MALE) },
                        fontFamily = deliusFontFamily,
                        color = Color.Black
                    )
                }
                Spacer(Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically){
                    RadioButton(
                        selected = gender == Gender.FEMALE,
                        onClick = {onGenderChange(Gender.FEMALE)},
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFFFF789B),
                            unselectedColor = Color.Black
                        )
                    )
                    Text(
                        text = "Female",
                        modifier = Modifier.clickable { onGenderChange(Gender.FEMALE) },
                        fontFamily = deliusFontFamily,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        CommonDivider()

        Spacer(Modifier.height(8.dp))

        Column(Modifier.fillMaxWidth()) {
            Text("I'm looking for:", fontFamily = deliusFontFamily, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically){
                    RadioButton(
                        selected = genderPreference == Gender.MALE,
                        onClick = {onGenderPreferenceChange(Gender.MALE)},
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFFFF789B),
                            unselectedColor = Color.Black
                        )
                    )
                    Text(
                        text = "Male",
                        modifier = Modifier.clickable { onGenderPreferenceChange(Gender.MALE) },
                        fontFamily = deliusFontFamily,
                        color = Color.Black
                    )
                }
                Spacer(Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically){
                    RadioButton(
                        selected = genderPreference == Gender.FEMALE,
                        onClick = {onGenderPreferenceChange(Gender.FEMALE)},
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFFFF789B),
                            unselectedColor = Color.Black
                        )
                    )
                    Text(
                        text = "Female",
                        modifier = Modifier.clickable { onGenderPreferenceChange(Gender.FEMALE) },
                        fontFamily = deliusFontFamily,
                        color = Color.Black
                    )
                }
                Spacer(Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically){
                    RadioButton(
                        selected = genderPreference == Gender.ANY,
                        onClick = {onGenderPreferenceChange(Gender.ANY)},
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFFFF789B),
                            unselectedColor = Color.Black
                        )
                    )
                    Text(
                        text = "Any",
                        modifier = Modifier.clickable { onGenderPreferenceChange(Gender.ANY) },
                        fontFamily = deliusFontFamily,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        CommonDivider()
        Spacer(Modifier.height(8.dp))

        // --- Autocomplete City Search Bar from FTSProfileScreen ---
        CityAutocompleteTextField(
            vm = vm,
            onCitySelected = onCitySelected,
            onCityInputChanged = onCityInputChanged,
            cityInputValue = cityInput
        )

        Spacer(Modifier.height(8.dp))
        CommonDivider()
        Spacer(Modifier.height(8.dp))
        InterestsSelector(
            selectedInterests = interests,
            onInterestsChange = onInterestsChange
        )
        Spacer(Modifier.height(16.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(25.dp),
            contentPadding = PaddingValues(),
            onClick = { showChangePasswordDialog = true }
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF744D8C),
                                Color(0xFF9C27B0)
                            )
                        ),
                        shape = RoundedCornerShape(25.dp)
                    )
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Change Password",
                    color = Color.White,
                    fontFamily = deliusFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 18.sp
                )
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(25.dp),
            contentPadding = PaddingValues(),
            onClick = { onLogout.invoke() }
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFF789B),
                                Color(0xFFD7274E)
                            )
                        ),
                        shape = RoundedCornerShape(25.dp)
                    )
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Logout",
                    color = Color.White,
                    fontFamily = deliusFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(25.dp),
            contentPadding = PaddingValues(),
            onClick = { showDeleteAccountDialog = true }
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Red.copy(alpha = 0.6f),
                                Color(0xFF9F2929)
                            )
                        ),
                        shape = RoundedCornerShape(25.dp)
                    )
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Delete Account",
                    color = Color.White,
                    fontFamily = deliusFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun ProfileImage(imageUrl : String?,vm: TCViewModel) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ){
            uri:Uri? ->
        uri?.let {vm.uploadProfileImage(uri)}
    }

    Box(modifier = Modifier.height(IntrinsicSize.Min).padding(0.dp)) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .padding(top = 16.dp, bottom = 16.dp)
                .fillMaxWidth()
                .clickable {
                    launcher.launch("image/*")
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(150.dp)
                    .border(
                        width = 5.dp,
                        color = Color(0xFF744D8C),
                        shape = CircleShape
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6))
            ) {
                CommonImage(data = imageUrl)
            }
            Text(
                "Change profile picture",
                fontFamily = deliusFontFamily,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        val isLoading = vm.inProgress.value
        if(isLoading){
            CommonProgressSpinner()
        }
    }
}

@Composable
fun InterestsSelector(
    selectedInterests: List<String>,
    onInterestsChange: (List<String>) -> Unit
) {
    val viewModel: TCViewModel = hiltViewModel()

    val allInterests by viewModel.allInterests.collectAsState()
    val interestsLoaded by viewModel.interestsLoaded.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<InterestData>>(emptyList()) }
    var validationResult by remember { mutableStateOf<FinalInterestValidator.Result?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadAllInterests()
    }

    // THÊM DEBUG LOG NÀY
    LaunchedEffect(allInterests) {
        android.util.Log.d("InterestSelector", "allInterests size: ${allInterests.size}")
    }

    LaunchedEffect(suggestions) {
        android.util.Log.d("InterestSelector", "suggestions size: ${suggestions.size}")
    }

    LaunchedEffect(searchQuery, allInterests) {
        if (searchQuery.isEmpty()) {
            suggestions = allInterests.take(10)
            validationResult = null
        } else {
            val lower = searchQuery.lowercase()
            suggestions = allInterests
                .filter { it.name.lowercase().contains(lower) }
                .take(10)
        }
    }

    Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
        Text("Interests", fontFamily = deliusFontFamily, color = Color.Black)

        Spacer(Modifier.height(8.dp))

        // Search TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                validationResult = null
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Input name of interest", fontFamily = deliusFontFamily, color = Color.Black) },
            leadingIcon = {
                Icon(androidx.compose.material.icons.Icons.Default.Search, null, tint = Color.Black)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Close, null, tint = Color.Black)
                    }
                }
            },
            singleLine = true,
            textStyle = TextStyle(fontFamily = deliusFontFamily, color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                cursorColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(Modifier.height(8.dp))

        // Suggestions dropdown
        if (suggestions.isNotEmpty() && validationResult == null && searchQuery.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    suggestions.forEach { interest ->
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!selectedInterests.contains(interest.name)) {
                                            onInterestsChange(selectedInterests + interest.name)
                                            viewModel.incrementInterestUsage(interest.id)
                                        }
                                        searchQuery = ""
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(interest.name, fontWeight = FontWeight.Bold, fontFamily = deliusFontFamily, color = Color.Black)
                                    Text(interest.category, fontSize = 12.sp, color = Color.Black, fontFamily = deliusFontFamily)
                                }
                            }
                            CommonDivider()
                        }
                    }
                }
            }
        }

        // Add new button - LUÔN HIỂN THỊ khi đang gõ
        if (searchQuery.length >= 2 && validationResult == null) {
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F7))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                val trimmed = searchQuery.trim()
                                val exactMatch = allInterests.find { it.name.equals(trimmed, ignoreCase = true) }

                                if (exactMatch != null) {
                                    // Nếu trùng khớp chính xác, thêm luôn vào danh sách đã chọn
                                    if (!selectedInterests.contains(exactMatch.name)) {
                                        onInterestsChange(selectedInterests + exactMatch.name)
                                        viewModel.incrementInterestUsage(exactMatch.id)
                                    }
                                    searchQuery = ""
                                    validationResult = null
                                } else {
                                    // Các trường hợp còn lại đều coi là NewInterest
                                    val capitalized = trimmed.split(" ")
                                        .joinToString(" ") { token -> token.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }

                                    validationResult = FinalInterestValidator.Result.NewInterest(
                                        name = capitalized,
                                        needsReview = true
                                    )
                                }
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.Add,
                        null,
                        tint = Color(0xFFFF789B)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Add \"$searchQuery\" as new interest",
                        color = Color(0xFFFF789B),
                        fontWeight = FontWeight.Bold,
                        fontFamily = deliusFontFamily
                    )
                }
            }
        }

        // New interest
        AnimatedVisibility(validationResult is FinalInterestValidator.Result.NewInterest) {
            (validationResult as? FinalInterestValidator.Result.NewInterest)?.let { new ->
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F5))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ready to add:", fontWeight = FontWeight.Bold, fontFamily = deliusFontFamily, color = Color(0xFFFF789B))
                        Text(new.name, fontSize = 18.sp, fontFamily = deliusFontFamily, color = Color(0xFF744D8C), fontWeight = FontWeight.Bold)
                        if (new.needsReview) {
                            Text(
                                "Will be reviewed by an admin later",
                                fontSize = 12.sp,
                                color = Color(0xFF744D8C),
                                fontFamily = deliusFontFamily
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.addNewInterest(new.name) { newInterest ->
                                    onInterestsChange(selectedInterests + newInterest.name)
                                    searchQuery = ""
                                    validationResult = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFFF789B),
                                                Color(0xFFD7274E)
                                            )
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Add \"${new.name}\"", fontFamily = deliusFontFamily, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Selected interests chips
        if (selectedInterests.isNotEmpty()) {
            Text("Selected: ${selectedInterests.size}", fontSize = 14.sp, color = Color.Black, fontFamily = deliusFontFamily)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedInterests.forEach { name ->
                    FilterChip(
                        selected = true,
                        onClick = { onInterestsChange(selectedInterests - name) },
                        label = { Text(name, fontFamily = deliusFontFamily, color = Color.White) },
                        trailingIcon = {
                            Icon(
                                androidx.compose.material.icons.Icons.Default.Close,
                                null,
                                Modifier.size(16.dp),
                                tint = Color.White
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF789B),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }
        }
        if (!interestsLoaded) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFF789B),
                trackColor = Color(0xFFFFC1CC)
            )
        }
    }
}