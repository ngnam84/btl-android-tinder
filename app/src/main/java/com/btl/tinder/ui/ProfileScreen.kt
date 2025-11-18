package com.btl.tinder.ui

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
import androidx.navigation.NavController
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
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.btl.tinder.DestinationScreen
import com.btl.tinder.navigateTo
import com.google.android.gms.common.internal.service.Common
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
import com.google.android.material.chip.Chip
import androidx.compose.foundation.layout.FlowRow
import androidx.wear.compose.material.ChipDefaults
import androidx.compose.material3.FilterChip

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
        else userData!!.gender!!.uppercase()
        val gpreper = if(userData?.genderPreference.isNullOrEmpty()) "FEMALE"
        else userData!!.genderPreference!!.uppercase()
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

        val scrollState = rememberScrollState()
        Column(modifier = Modifier.background(Color.White)){
            ProfileContent(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(top = 32.dp),
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
    Column(modifier = modifier){
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
    val allInterests = listOf(
        "Bóng đá", "Bóng rổ", "Cầu lông", "Tennis",
        "Chạy bộ", "Yoga", "Đọc sách", "Du lịch",
        "Nấu ăn", "Game", "Nhạc", "Phim ảnh"
    )

    Column(modifier = Modifier.padding(8.dp)) {

        Text("Chọn sở thích", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        FlowRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),            //space between items
            verticalArrangement = Arrangement.spacedBy(12.dp)               //space between cac lines
        ) {
            allInterests.forEach { interestitem ->
                val isSelected = selectedInterests.contains(interestitem)

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newInterests = if (isSelected)
                            selectedInterests - interestitem
                        else
                            selectedInterests + interestitem
                        onInterestsChange(newInterests)
                    },
                    label = { Text(interestitem) }
                )
            }
        }

        Text(
            text = "Đã chọn: ${selectedInterests.size}",
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
