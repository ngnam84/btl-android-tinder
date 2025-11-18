package com.btl.tinder

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TCApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Stream Video sẽ được khởi tạo trong VideoCallScreen với user và token
        // Không khởi tạo ở đây để tránh conflict
    }
}
