package com.btl.tinder.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.btl.tinder.CommonDivider
import com.btl.tinder.CommonImage
import com.btl.tinder.CommonProgressSpinner
import com.btl.tinder.DestinationScreen
import com.btl.tinder.TCViewModel
import com.btl.tinder.data.CityData
import com.btl.tinder.data.Event
import com.btl.tinder.navigateTo
import com.btl.tinder.ui.theme.deliusFontFamily
import com.btl.tinder.ui.theme.pacificoFontFamily
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FTSProfileScreen(navController: NavController, vm: TCViewModel) {

    val systemUiController = rememberSystemUiController()
    val context = LocalContext.current

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = true
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent
        )
    }

    var name by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf(Gender.MALE) }
    var genderPreference by rememberSaveable { mutableStateOf(Gender.FEMALE) }
    var selectedCity by remember { mutableStateOf<CityData?>(null) }
    var cityInput by rememberSaveable { mutableStateOf("") }
    var interests by rememberSaveable { mutableStateOf(listOf<String>()) }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            val gradientBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFFF789B),
                    Color(0xFFD7274E)
                )
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Black)) {
                        append("Tell something \nabout ")
                    }
                    withStyle(style = SpanStyle(brush = gradientBrush)) {
                        append("yourself")
                    }
                },
                modifier = Modifier
                    .padding(16.dp),
                fontSize = 50.sp,
                fontFamily = pacificoFontFamily,
                lineHeight = 75.sp
            )

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                ProfileImage1(imageUrl = vm.userData.value?.imageUrl, vm = vm)

                Spacer(modifier = Modifier.height(16.dp).background(Color.Transparent))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", fontFamily = deliusFontFamily, color = Color.Black) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = deliusFontFamily, color = Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(8.dp).background(Color(0xFFFF768B)))

                // --- Autocomplete City Search Bar ---
                CityAutocompleteTextField(
                    vm = vm,
                    onCitySelected = {
                        selectedCity = it
                        cityInput = it?.city ?: ""
                    },
                    onCityInputChanged = { cityInput = it },
                    cityInputValue = cityInput
                )

                Spacer(modifier = Modifier.height(8.dp).background(Color(0xFFFF768B)))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio", fontFamily = deliusFontFamily, color = Color.Black) },
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    textStyle = TextStyle(fontFamily = deliusFontFamily, color = Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                CommonDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Gender Selection
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("I am a:", fontFamily = deliusFontFamily, color = Color.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = gender == Gender.MALE,
                                onClick = { gender = Gender.MALE },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFFFF789B),
                                    unselectedColor = Color.Black
                                )
                            )
                            Text(
                                text = "Male",
                                modifier = Modifier.clickable { gender = Gender.MALE },
                                fontFamily = deliusFontFamily,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = gender == Gender.FEMALE,
                                onClick = { gender = Gender.FEMALE },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFFFF789B),
                                    unselectedColor = Color.Black
                                )
                            )
                            Text(
                                text = "Female",
                                modifier = Modifier.clickable { gender = Gender.FEMALE },
                                fontFamily = deliusFontFamily,
                                color = Color.Black
                            )
                        }
                    }
                }


                Spacer(modifier = Modifier.height(8.dp))
                CommonDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Column(Modifier.fillMaxWidth()) {
                    Text(
                        "I'm looking for:",
                        fontFamily = deliusFontFamily,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = genderPreference == Gender.MALE,
                                onClick = { genderPreference = Gender.MALE },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFFFF789B),
                                    unselectedColor = Color.Black
                                )
                            )
                            Text(
                                text = "Male",
                                modifier = Modifier.clickable { genderPreference = Gender.MALE },
                                fontFamily = deliusFontFamily,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = genderPreference == Gender.FEMALE,
                                onClick = { genderPreference = Gender.FEMALE },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFFFF789B),
                                    unselectedColor = Color.Black
                                )
                            )
                            Text(
                                text = "Female",
                                modifier = Modifier.clickable { genderPreference = Gender.FEMALE },
                                fontFamily = deliusFontFamily,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = genderPreference == Gender.ANY,
                                onClick = { genderPreference = Gender.ANY },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFFFF789B),
                                    unselectedColor = Color.Black
                                )
                            )
                            Text(
                                text = "Any",
                                modifier = Modifier.clickable { genderPreference = Gender.ANY },
                                fontFamily = deliusFontFamily,
                                color = Color.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                CommonDivider()
                Spacer(modifier = Modifier.height(8.dp))

                InterestsSelector(
                    selectedInterests = interests,
                    onInterestsChange = { interests = it }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(25.dp),
                contentPadding = PaddingValues(),
                onClick = {
                    val imageUrl = vm.userData.value?.imageUrl
                    if (name.isBlank() || bio.isBlank() || imageUrl.isNullOrBlank()) {
                        vm.popupNotification.value =
                            Event("Please fill all fields and upload a profile picture.")
                    } else {
                        coroutineScope.launch {
                            val addressToSave = selectedCity?.city ?: cityInput.ifBlank { null }
                            val latToSave = selectedCity?.lat
                            val longToSave = selectedCity?.lng

                            vm.updateProfileData(
                                name = name,
                                username = vm.userData.value?.username ?: "",
                                bio = bio,
                                gender = gender,
                                genderPreference = genderPreference,
                                interests = interests,
                                address = addressToSave,
                                lat = latToSave,
                                long = longToSave,
                                ftsComplete = true
                            )

                            navigateTo(navController, DestinationScreen.Swipe.route)
                        }
                    }
                }
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
                        text = "Continue",
                        color = Color.White,
                        fontFamily = deliusFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileImage1(imageUrl : String?,vm: TCViewModel) {
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

    Box(modifier = Modifier.height(IntrinsicSize.Min).padding(0.dp)) {
        Column(modifier = Modifier.padding(8.dp).padding(top = 16.dp, bottom = 16.dp).fillMaxWidth().clickable{
            launcher.launch("image/*")
        },horizontalAlignment = Alignment.CenterHorizontally)
        {
            Card(shape = CircleShape,modifier = Modifier.padding(8.dp).size(200.dp)
                .border(
                    width = 5.dp,
                    color = Color(0xFF744D8C),
                    shape = CircleShape
                ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)) ){
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityAutocompleteTextField(
    vm: TCViewModel,
    onCitySelected: (CityData?) -> Unit,
    onCityInputChanged: (String) -> Unit,
    cityInputValue: String
) {
    val allCities by vm.cities.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Case-insensitive filtering
    val cities = remember(cityInputValue, allCities) {
        if (cityInputValue.isBlank()) emptyList()
        else allCities.filter {
            it.city?.lowercase()?.contains(cityInputValue.lowercase()) == true
        }
    }

    LaunchedEffect(cityInputValue) {
        vm.searchCities(cityInputValue)
    }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().menuAnchor().onFocusChanged {

//                if (!it.isFocused && !expanded) {
//                    TODO()
//                }
            },
            value = cityInputValue,
            onValueChange = {
                onCityInputChanged(it)
                expanded = it.isNotEmpty()
            },
            label = { Text("City", fontFamily = deliusFontFamily, color = Color.Black) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            textStyle = TextStyle(fontFamily = deliusFontFamily, color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black, unfocusedBorderColor = Color.Black, cursorColor = Color.Black,
                focusedLabelColor = Color.Black, unfocusedLabelColor = Color.Black
            )
        )

        if (cities.isNotEmpty()) {
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                cities.forEach { cityData ->
                    DropdownMenuItem(
                        text = { Text("${cityData.city}, ${cityData.country}", fontFamily = deliusFontFamily) },
                        onClick = {
                            onCitySelected(cityData)
                            expanded = false
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }
    }
}