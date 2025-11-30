package com.btl.tinder.ui
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding

enum class Gender {
    MALE, FEMALE, ANY
}

@Composable
fun ProfileScreen(navController: NavController, vm: TCViewModel) {
    val inProgress = vm.inProgress.value

    if (inProgress)
        CommonProgressSpinner()
    else{
        val userData = vm.userData.value
        val g = if(userData?.gender.isNullOrEmpty()) "MALE"
        else userData.gender!!.uppercase()
        val gpreper = if(userData?.genderPreference.isNullOrEmpty()) "FEMALE"
        else userData.genderPreference!!.uppercase()
        var name by rememberSaveable { mutableStateOf(userData?.name ?: "") }
        var username by rememberSaveable { mutableStateOf(userData?.username ?: "") }
        var bio by rememberSaveable { mutableStateOf(userData?.bio ?: "") }
        var gender by rememberSaveable {
            mutableStateOf(Gender.valueOf(g))
        }
        var genderPreference by rememberSaveable {
            mutableStateOf(Gender.valueOf(gpreper))
        }
        var interests by rememberSaveable { mutableStateOf(userData?.interests ?: listOf())}

        Column(modifier = Modifier
            .background(Color.White)
            .statusBarsPadding()
            .fillMaxSize()
        ){
            ProfileContent(
                modifier = Modifier
                    .weight(1f),
                vm=vm,
                name=name,
                username=username,
                bio = bio,
                gender = gender,
                genderPreference = genderPreference,
                interests = interests,
                onNameChange = {name = it},
                onUsernameChange = {username = it},
                onBioChange = { bio = it},
                onGenderChange = {gender = it},
                onGenderPreferenceChange = {genderPreference=it},
                onInterestsChange = {interests = it},
                onSave = {
                    //vm.updateProfileData(name,username,bio,gender,genderPreference,interests)
                    //vm.updateProfileData(name,username,bio,gender,genderPreference)
                },
                onBack = { navigateTo(navController, DestinationScreen.Swipe.route) },
                onLogout = {
                    vm.onLogout()
                    navigateTo(navController, DestinationScreen.Login.route)
                }
            )
            BottomNavigationMenu(
                BottomNavigationItem.PROFILE,
                navController
            )
        }
    }
}

@Composable
fun ProfileContent(
    modifier:Modifier,
    vm: TCViewModel,

    name: String,
    username: String,
    bio: String,
    gender: Gender,
    genderPreference: Gender,
    interests: List<String>,
    onNameChange:(String) -> Unit,
    onUsernameChange:(String) -> Unit,
    onBioChange:(String) -> Unit,
    onGenderChange:(Gender) -> Unit,
    onGenderPreferenceChange:(Gender) -> Unit,
    onInterestsChange:(List<String>) -> Unit,
    onSave: () -> Unit,
    onBack :() -> Unit,
    onLogout : () -> Unit

){
    val imageUrl = vm.userData.value?.imageUrl
    val scrollState = rememberScrollState()

    Column(modifier = modifier
        .verticalScroll(scrollState)
        .navigationBarsPadding()
        .imePadding()
    ){
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text("Back",Modifier.clickable {onBack.invoke()})
            Text("Save",Modifier.clickable {onSave.invoke()})
        }

        CommonDivider()

        ProfileImage(imageUrl = imageUrl,vm = vm)
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp), verticalAlignment = Alignment.CenterVertically)
        {
            Text("Username",modifier = Modifier.width(100.dp))
            TextField(
                value = username,
                onValueChange = onUsernameChange,
                colors = TextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )
        }

        CommonDivider()

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp), verticalAlignment = Alignment.CenterVertically)
        {
            Text("Name",modifier = Modifier.width(100.dp))
            TextField(
                value = name,
                onValueChange = onNameChange,
                colors = TextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp), verticalAlignment = Alignment.CenterVertically)
        {
            Text("Bio",modifier = Modifier.width(100.dp))
            TextField(
                value = bio,
                onValueChange = onBioChange,
                colors = TextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black),
                singleLine = false
            )
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp), verticalAlignment = Alignment.CenterVertically)
        {
            Text("I am a ", modifier = Modifier
                .width(100.dp)
                .padding(8.dp))
            Column(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically){
                    RadioButton(
                        selected = gender ==Gender.MALE,
                        onClick = {onGenderChange(Gender.MALE) })
                    Text(
                        text = "Man",
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onGenderChange(Gender.MALE) })

                }
                Row(verticalAlignment = Alignment.CenterVertically){
                    RadioButton(
                        selected = gender ==Gender.FEMALE,
                        onClick = {onGenderChange(Gender.FEMALE) })
                    Text(
                        text = "Girl",
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onGenderChange(Gender.FEMALE) })

                }
            }
        }

        CommonDivider()

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp), verticalAlignment = Alignment.CenterVertically)
        {
            Text("I'm looking for", modifier = Modifier
                .width(100.dp)
                .padding(8.dp))
            Column(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically){
                    RadioButton(
                        selected = genderPreference ==Gender.MALE,
                        onClick = {onGenderPreferenceChange(Gender.MALE) })
                    Text(
                        text = "Male",
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onGenderPreferenceChange(Gender.MALE) })

                }
                Row(verticalAlignment = Alignment.CenterVertically){
                    RadioButton(
                        selected = genderPreference ==Gender.FEMALE,
                        onClick = {onGenderPreferenceChange(Gender.FEMALE) })
                    Text(
                        text = "Female",
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onGenderPreferenceChange(Gender.FEMALE) })

                }
                Row(verticalAlignment = Alignment.CenterVertically){
                    RadioButton(
                        selected = genderPreference ==Gender.ANY,
                        onClick = {onGenderPreferenceChange(Gender.ANY) })
                    Text(
                        text = "Any",
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onGenderPreferenceChange(Gender.ANY) })

                }
            }
        }

        CommonDivider()
        InterestsSelector(
            selectedInterests = interests,
            onInterestsChange = onInterestsChange
        )
        CommonDivider()
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center)
        {
            Button(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp)
                    .clickable { onLogout.invoke() },
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
//            Text("Logout",Modifier.clickable {onLogout.invoke()})
        }
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

    Box(modifier = Modifier
        .height(IntrinsicSize.Min)
        .padding(0.dp)
        .meshGradient(
            points = listOf(
                listOf(
                    Offset(0f, 0f) to Color(0xFFFFB3C6),
                    Offset(.5f, 0f) to Color(0xFFFFB3C6),
                    Offset(1f, 0f) to Color(0xFFFFB3C6),
                ),
                listOf(
                    Offset(0f, .5f) to Color(0xFFFF7898),
                    Offset(.5f, .9f) to Color(0xFFFF7898),
                    Offset(1f, .5f) to Color(0xFFFF7898),
                ),
                listOf(
                    Offset(0f, 1f) to Color(0xFFF83460),
                    Offset(.5f, 1f) to Color(0xFFF83460),
                    Offset(1f, 1f) to Color(0xFFF83460),
                ),
            ),
        )) {
        Column(modifier = Modifier
            .padding(8.dp)
            .padding(top = 16.dp, bottom = 16.dp)
            .fillMaxWidth()
            .clickable {
                launcher.launch("image/*")
            },horizontalAlignment = Alignment.CenterHorizontally)
        {
            Card(shape = CircleShape,modifier = Modifier
                .padding(8.dp)
                .size(100.dp)){
                CommonImage(data = imageUrl)
            }
            Text("Change profile picture", fontFamily = deliusFontFamily, color = Color.Black, fontWeight = FontWeight.Bold)
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: TCViewModel = androidx.hilt.navigation.compose.hiltViewModel()

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

    Column(modifier = Modifier.padding(8.dp)) {
        Text("Interests", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = deliusFontFamily, color = Color.Black)

        Spacer(Modifier.height(8.dp))

        // Search TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                validationResult = null
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Input name of interest", fontFamily = deliusFontFamily, color = Color.Gray) },
            leadingIcon = {
                Icon(androidx.compose.material.icons.Icons.Default.Search, null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Close, null)
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(Modifier.height(8.dp))

        // Suggestions dropdown
        if (suggestions.isNotEmpty() && validationResult == null && searchQuery.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
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
                                    Text(interest.category, fontSize = 12.sp, color = Color.Gray, fontFamily = deliusFontFamily)
                                }
                            }
                            CommonDivider()
                        }
                    }

                    // Add new button
                    if (searchQuery.length >= 2) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        val result = FinalInterestValidator.validate(
                                            searchQuery,
                                            allInterests
                                        )
                                        validationResult = result
                                    }
                                }
                                .padding(12.dp)
                        ) {
                            Icon(
                                androidx.compose.material.icons.Icons.Default.Add,
                                null,
                                tint = Color(0xFFFF7898)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Add \"$searchQuery\"",
                                color = Color(0xFFFF7898),
                                fontWeight = FontWeight.Bold,
                                fontFamily = deliusFontFamily
                            )
                        }
                    }
                }
            }
        }

        // Typo suggestion
        AnimatedVisibility(validationResult is FinalInterestValidator.Result.TypoSuggestion) {
            (validationResult as? FinalInterestValidator.Result.TypoSuggestion)?.let { typo ->
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E6))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Did you mean:", fontSize = 12.sp, color = Color.Gray, fontFamily = deliusFontFamily)
                        Text(
                            typo.suggested.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF7898),
                            fontFamily = deliusFontFamily
                        )
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Button(
                                onClick = {
                                    if (!selectedInterests.contains(typo.suggested.name)) {
                                        onInterestsChange(selectedInterests + typo.suggested.name)
                                        viewModel.incrementInterestUsage(typo.suggested.id)
                                    }
                                    searchQuery = ""
                                    validationResult = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7898))
                            ) {
                                Text("Yes, that's it", fontFamily = deliusFontFamily)
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(onClick = {
                                viewModel.addNewInterest(typo.original) { newInterest ->
                                    onInterestsChange(selectedInterests + newInterest.name)
                                    searchQuery = ""
                                    validationResult = null
                                }
                            }) {
                                Text("No, add \"${typo.original}\"", fontFamily = deliusFontFamily)
                            }
                        }
                    }
                }
            }
        }

        // New interest
        AnimatedVisibility(validationResult is FinalInterestValidator.Result.NewInterest) {
            (validationResult as? FinalInterestValidator.Result.NewInterest)?.let { new ->
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ready to add:", fontWeight = FontWeight.Bold, fontFamily = deliusFontFamily, color = Color.Black)
                        Text(new.name, fontSize = 18.sp, fontFamily = deliusFontFamily, color = Color.Black)
                        if (new.needsReview) {
                            Text(
                                "Will be reviewed by an admin later",
                                fontSize = 12.sp,
                                color = Color.Gray,
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Add \"${new.name}\"", fontFamily = deliusFontFamily)
                        }
                    }
                }
            }
        }

        // Invalid
        AnimatedVisibility(validationResult is FinalInterestValidator.Result.Invalid) {
            (validationResult as? FinalInterestValidator.Result.Invalid)?.let { invalid ->
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Invalid:", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), fontFamily = deliusFontFamily)
                        Text(invalid.reason, fontSize = 14.sp, fontFamily = deliusFontFamily, color = Color.Black)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                searchQuery = ""
                                validationResult = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Try again", fontFamily = deliusFontFamily)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Selected interests chips
        if (selectedInterests.isNotEmpty()) {
            Text("Selected: ${selectedInterests.size}", fontSize = 14.sp, color = Color.Gray, fontFamily = deliusFontFamily)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedInterests.forEach { name ->
                    FilterChip(
                        selected = true,
                        onClick = { onInterestsChange(selectedInterests - name) },
                        label = { Text(name, fontFamily = deliusFontFamily) },
                        trailingIcon = {
                            Icon(
                                androidx.compose.material.icons.Icons.Default.Close,
                                null,
                                Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
        if (!interestsLoaded) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
}