package com.btl.tinder

// https://mvnrepository.com/artifact/org.json/json
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import es.dmoral.toasty.Toasty
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject


/**
 * Điều hướng đến một màn hình cụ thể trong ứng dụng thông qua [NavController].
 *
 * Hàm này chuyển đến route được chỉ định và đảm bảo rằng:
 * - Nếu màn hình (route) đó đã có trong back stack, nó sẽ không được tạo thêm (nhờ `launchSingleTop = true`).
 * - Các màn hình nằm phía trên route đó trong back stack sẽ bị xóa (nhờ `popUpTo(route)`),
 *   giúp tránh trùng lặp và giữ ngăn xếp điều hướng gọn gàng.
 *
 * @param navController Bộ điều khiển điều hướng dùng để thực hiện chuyển màn hình.
 * @param route Đường dẫn (route) của màn hình đích cần điều hướng tới.
 */

fun navigateTo(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(route)
        launchSingleTop = true
    }
}

@Composable
fun CommonProgressSpinner() {
    Row(
        modifier = Modifier
            .alpha(0.5f)
            .background(Color.LightGray)
            .clickable(enabled = false) {}
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun NotificationMessage(vm: TCViewModel) {
    val notifState = vm.popupNotification.value
    val notifMessage = notifState?.getContentOrNull()
    val icon = ContextCompat.getDrawable(LocalContext.current, R.drawable.logo_main)
    if (!notifMessage.isNullOrEmpty()) {
        Toasty.normal(LocalContext.current, notifMessage, Toasty.LENGTH_LONG, icon).show()
    }
}

@Composable
fun CheckSignedIn(vm: TCViewModel, navController: NavController) {
    val alreadyLoggedIn = remember { mutableStateOf(false) }
    val signedIn = vm.signInState.value
    if (signedIn == SignInState.SIGNED_IN_FROM_LOGIN && !alreadyLoggedIn.value) {
        alreadyLoggedIn.value = true
        navController.navigate(DestinationScreen.Profile.route) {
            popUpTo(0)
        }
    }
    if(signedIn == SignInState.SIGNED_IN_FROM_SIGNUP && !alreadyLoggedIn.value) {
        alreadyLoggedIn.value = true
        navController.navigate(DestinationScreen.FTSetup.route) {
            popUpTo(0)
        }
    }
}

@Composable
fun CommonDivider(){
    Divider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier
            .alpha(0.3f)
            .padding(top = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun CommonImage(
    data: String?,
    modifier: Modifier = Modifier.wrapContentSize(),
    contentScale : ContentScale = ContentScale.Crop
){
    SubcomposeAsyncImage(
        model = data,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale,
    ){
        val state = painter.state
        if(state is AsyncImagePainter.State.Loading){
            CommonProgressSpinner()
        }
        else{
            SubcomposeAsyncImageContent()
        }
    }
}

@Composable
fun GeoCoder(
    add: String
) {
    val client = OkHttpClient()
    try {
        val url = "https://api.geoapify.com/v1/geocode/search".toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("text", add)
            .addQueryParameter("apiKey", "eedd1bc6f483429793b03110c4f4e9ce")
            .build()

        val request: Request = Request.Builder().url(url).build()
        val response: Response = client.newCall(request).execute()
        if (response.code == 200) {
            val json = JSONObject(response.body!!.string())

            val results = json.getJSONArray("features")
            val firstResult = results.getJSONObject(0)
            val firstResultProperties = firstResult.getJSONObject("properties")

            val formattedAddress = firstResultProperties.getString("formatted")
            Log.e("GeoCoder", "GeoCoder: $formattedAddress")
        } else {
//            System.err.println("Request error " + response.code)
//            System.err.println(response.body!!.string())
            Log.e("GeoCoder", "Request error " + response.code)
            Log.e("GeoCoder", response.body!!.string())
        }
    } catch( e: Exception ) {

    }
}

