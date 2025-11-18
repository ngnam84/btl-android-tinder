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

    // --- Jetpack Compose ---
    val composeBom = platform("androidx.compose:compose-bom:2025.10.00")
    implementation("androidx.compose:compose-bom:2025.11.00")
    androidTestImplementation("androidx.compose:compose-bom:2025.11.00")

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // --- AndroidX Core ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.firebase.functions.ktx)
    implementation(libs.firebase.appcheck.debug)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.compose.material)

    // --- Navigation ---
    implementation(libs.androidx.navigation.compose)

    // --- Firebase ---
    implementation(platform(libs.firebase.bom)) // BOM quản lý version tất cả Firebase
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.functions.ktx.v2121)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation("com.google.firebase:firebase-analytics:23.0.0")
    implementation ("com.google.firebase:firebase-appcheck-debug")

    // --- Hilt / Dependency Injection ---
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // --- Coil (Image Loading) ---
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // --- Coroutines ---
    implementation(libs.kotlinx.coroutines.android)

    // --- Accompanist ---
    implementation(libs.accompanist.systemuicontroller)

    // --- Stream Chat ---
    implementation(libs.stream.chat.android.offline)
    implementation(libs.stream.chat.android.compose)

    // --- Credentials / Google Sign-In ---
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // --- Utility / UI ---
    implementation(libs.foundation)
    implementation(libs.toasty)
    implementation(libs.animated.navigation.bar)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
