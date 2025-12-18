package com.hiendao.data.remote.retrofit.voice

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface VoiceApi {
    @Multipart
    @POST("voice/create")
    suspend fun createVoice(
        @Part("name") name: RequestBody,
        @Part audio: MultipartBody.Part
    ): String
}
