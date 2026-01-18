package com.hiendao.storyapp

import android.app.Application
import com.facebook.FacebookSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        FacebookSdk.sdkInitialize(this)
        super.onCreate()
    }
}