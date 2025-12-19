package com.hiendao.domain.repository

import android.net.http.HttpException
import com.hiendao.data.remote.retrofit.login.LoginAPI
import com.hiendao.data.remote.retrofit.login.model.LoginResponse
import com.hiendao.domain.utils.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor(
    private val loginAPI: LoginAPI
){
    suspend fun loginWithGoogle(token: String): Flow<Response<LoginResponse>> {
        return flow {
            try {
                emit(Response.Loading)
                val result = loginAPI.loginWithGoogle(token)
                if(!result.accessToken.isNullOrEmpty() && !result.refreshToken.isNullOrEmpty()){
                    emit(Response.Success(result))
                } else if(!result.message.isNullOrEmpty()){
                    emit(Response.Error("Login failed", Exception(result.message.toString())))
                }
            } catch (e : Exception){
                emit(Response.Error(e.message.toString(), e))
            }
        }
    }

    suspend fun refreshToken(refreshToken: String): Flow<Response<LoginResponse>> {
        return flow {
            try {
                emit(Response.Loading)
                val result = loginAPI.refreshToken(refreshToken)
                if(!result.accessToken.isNullOrEmpty() && !result.refreshToken.isNullOrEmpty()){
                    emit(Response.Success(result))
                } else if(!result.message.isNullOrEmpty()){
                    emit(Response.Error("Login failed", Exception(result.message.toString())))
                }
            } catch (e : Exception){
                emit(Response.Error(e.message.toString(), e))
            }
        }
    }
}