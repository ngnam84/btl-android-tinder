package com.btl.tinder.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.btl.tinder.NotificationMessage
import com.btl.tinder.TCViewModel
import com.btl.tinder.data.Event
import com.btl.tinder.navigateTo
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FTSProfileScreen(navController: NavController, vm: TCViewModel) {

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

    // Hoisted state for the form fields
    var name by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf(Gender.MALE) }
    var genderPreference by rememberSaveable { mutableStateOf(Gender.FEMALE) }

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
        Column(modifier = Modifier.fillMaxSize()) {
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
                    .statusBarsPadding()
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

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", fontFamily = deliusFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = deliusFontFamily, color = Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.DarkGray,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.DarkGray,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio", fontFamily = deliusFontFamily) },
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    textStyle = TextStyle(fontFamily = deliusFontFamily, color = Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.DarkGray,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.DarkGray,
                        unfocusedLabelColor = Color.Gray
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
                                    unselectedColor = Color.Gray
                                )
                            )
                            Text(
                                text = "Man",
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
                                    unselectedColor = Color.Gray
                                )
                            )
                            Text(
                                text = "Woman",
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

                // Gender Preference Selection
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
                                    unselectedColor = Color.Gray
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
                                    unselectedColor = Color.Gray
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
                                    unselectedColor = Color.Gray
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
                        vm.updateProfileData(
                            name = name,
                            username = vm.userData.value?.username ?: "",
                            bio = bio,
                            gender = gender,
                            genderPreference = genderPreference
                        )
                        navigateTo(navController, DestinationScreen.Swipe.route)
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
            Card(shape = CircleShape,modifier = Modifier.padding(8.dp).size(200.dp)){
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
