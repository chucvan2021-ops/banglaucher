package com.liquidos.launcher

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LiquidOSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Khởi tạo các cấu hình nền nếu cần (Analytics, Crashlytics, etc.)
    }
}
