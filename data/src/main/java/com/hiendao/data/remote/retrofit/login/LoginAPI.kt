package com.hiendao.data.remote.retrofit.login

import com.hiendao.data.remote.retrofit.login.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginAPI {

    @POST("auth/google")
    suspend fun loginWithGoogle(
        @Body token: String
    ): LoginResponse

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body refreshToken: String
    ): LoginResponse

    @POST("auth/logout")
    suspend fun logout(
        @Body refreshToken: String
    ): String
}