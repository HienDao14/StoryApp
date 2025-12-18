package com.hiendao.domain.repository

import com.hiendao.data.remote.retrofit.voice.VoiceApi
import com.hiendao.domain.model.CreateVoiceRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import javax.inject.Inject
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

interface VoiceRepository {
    suspend fun createVoice(request: CreateVoiceRequest): Result<String>
}

class VoiceRepositoryImpl @Inject constructor(
    private val voiceApi: VoiceApi
) : VoiceRepository {
    override suspend fun createVoice(request: CreateVoiceRequest): Result<String> {
        return try {
            val namePart = request.name.toRequestBody("text/plain".toMediaTypeOrNull())

            // Assuming audio/mp3 or audio/wav or similar. "audio/*" is generic.
            val requestFile = request.audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audio", request.audioFile.name, requestFile)

            val response = voiceApi.createVoice(namePart, audioPart)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}