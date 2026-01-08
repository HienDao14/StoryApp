package com.hiendao.storyapp.di

import com.hiendao.coreui.appPreferences.AppPreferences
import okhttp3.Interceptor
import okhttp3.Response

class MyInterceptor(private val appPreferences: AppPreferences): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = appPreferences.ACCESS_TOKEN.value
        println("Token: $token")
        val request = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}