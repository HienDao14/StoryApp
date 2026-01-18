package com.hiendao.data.remote.retrofit.login.model

data class LoginResponse(
    val accessToken: String? = null,
    val refreshToken: String?= null,
    val userId: String?=null,
    val status: Int?= null,
    val errorCode: String?= null,
    val error: String?= null,
    val message: String?= null,
    val path: String?= null,
    val timestamp: String?= null
)
