plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.btl.tinder"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.btl.tinder"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.firebase.functions.ktx.v2121)
    implementation(libs.androidx.compose.ui.graphics)

    // --- Compose BOM ---
    val composeBom = platform("androidx.compose:compose-bom:2025.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // --- Jetpack Compose ---
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(libs.androidx.ui.test.junit4)


    // --- Android core ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // --- Navigation ---
    implementation(libs.androidx.navigation.compose)

    // --- Hilt / Dependency Injection ---
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)


    // --- Firebase ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-analytics:23.0.0")
    implementation ("com.google.firebase:firebase-appcheck-debug")



    // --- Accompanist ---
    implementation(libs.accompanist.systemuicontroller)

    // --- Coil (Image Loading) ---
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // --- Coroutines ---
    implementation(libs.kotlinx.coroutines.android)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- exyte AndroidAnimatedNavigationBar ---
    implementation(libs.animated.navigation.bar)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.foundation)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth.ktx)

    implementation(libs.toasty)

    //--- Stream Chat ---
    implementation(libs.stream.chat.android.client)
    implementation(libs.stream.chat.android.state)
    implementation(libs.stream.chat.android.offline.v6270)
    implementation(libs.stream.chat.android.ui.components)
    // Stream Chat Compose UI
    implementation(libs.stream.chat.android.compose)



    
    // --- Stream Video ---
    implementation(libs.stream.video.android.ui.compose)


    // --- Credentials / Google Sign-In ---
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // --- Utility / UI ---
    implementation(libs.foundation)
    implementation(libs.toasty)
    implementation(libs.animated.navigation.bar)

}