package com.btl.tinder.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.btl.tinder.CheckSignedIn
import com.btl.tinder.CommonProgressSpinner
import com.btl.tinder.DestinationScreen
import com.btl.tinder.R
import com.btl.tinder.TCViewModel
import com.btl.tinder.navigateTo
import com.btl.tinder.ui.deliusFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, vm: TCViewModel) {
    CheckSignedIn(vm = vm, navController = navController)

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
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val emailState = remember { mutableStateOf(TextFieldValue()) }
            val passwordState = remember { mutableStateOf(TextFieldValue()) }
            val passwordVisible = remember { mutableStateOf(false) }

            val focus = LocalFocusManager.current

            Image(
                painter = painterResource(R.drawable.logo_sub_1_fixed),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(top = 40.dp)
                    .padding(start = 8.dp, end = 8.dp, bottom = 0.dp)
            )

            Text(
                text = "Login",
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                fontSize = 50.sp,
                fontFamily = pacificoFontFamily,
                color = Color.Black
            )

            OutlinedTextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                modifier = Modifier.padding(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                label = { Text(text = "Email", fontFamily = deliusFontFamily, color = Color.Black) })

            OutlinedTextField(
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                modifier = Modifier.padding(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                label = { Text(text = "Password", fontFamily = deliusFontFamily, color = Color.Black) },
                visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible.value)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    val description = if (passwordVisible.value) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                        Icon(imageVector = image, description, tint = Color.Black) // Changed icon color to black
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    focus.clearFocus(force = true)
                    vm.onLogin(
                        emailState.value.text,
                        passwordState.value.text
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(25.dp),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF7898),
                                    Color(0xFF744D8C)
                                )
                            ),
                            shape = RoundedCornerShape(25.dp)
                        )
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Log In",
                        fontFamily = deliusFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            Text(text = "New here? Go to signup",
                color = Color.Black,
                fontFamily = deliusFontFamily,
                fontWeight = FontWeight.W700,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { navigateTo(navController, DestinationScreen.Signup.route) }
            )
        }

        val isLoading = vm.inProgress.value
        if (isLoading)
            CommonProgressSpinner()
    }
}
