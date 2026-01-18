package com.hiendao.data.remote.retrofit.voice

import com.hiendao.data.remote.retrofit.book.model.SearchBooksBody
import com.hiendao.data.remote.retrofit.voice.model.GenerateVoiceBody
import com.hiendao.data.remote.retrofit.voice.model.TrainModelResponse
import com.hiendao.data.remote.retrofit.voice.model.VoiceResponse
import com.hiendao.data.remote.retrofit.voice.model.VoiceResponseItem
import com.hiendao.data.remote.retrofit.voice.model.VoiceStoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface VoiceApi {
    @Multipart
    @POST("ai/train-model")
    suspend fun createVoice(
        @Part("name") name: RequestBody,
        @Part audio: MultipartBody.Part,
        @Part("f0Method") f0Method: RequestBody,
        @Part("epochsNumber") epochsNumber: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("trainAt") trainAt: RequestBody
    ): TrainModelResponse

    @GET("ai/my-voices")
    suspend fun getMyVoices(): List<VoiceResponseItem>

    @POST("ai/generate-voice")
    suspend fun getVoiceStory(
        @Body generateVoiceBody: GenerateVoiceBody
    ): VoiceStoryResponse
}
