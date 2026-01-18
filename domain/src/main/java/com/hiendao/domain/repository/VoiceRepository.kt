package com.hiendao.domain.repository

import com.hiendao.data.remote.retrofit.voice.VoiceApi
import com.hiendao.data.remote.retrofit.voice.model.TrainModelResponse
import com.hiendao.domain.model.CreateVoiceRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import javax.inject.Inject
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

interface VoiceRepository {
    suspend fun createVoice(request: CreateVoiceRequest): Result<TrainModelResponse>
}

class VoiceRepositoryImpl @Inject constructor(
    private val voiceApi: VoiceApi
) : VoiceRepository {
    override suspend fun createVoice(request: CreateVoiceRequest): Result<TrainModelResponse> {
        return try {
            val namePart = request.name.toRequestBody(null)
            val f0MethodPart = request.f0Method.toRequestBody(null)
            val epochsNumberPart = request.epochsNumber.toRequestBody(null)
            val userIdPart = request.userId.toRequestBody(null)
            val trainAtPart = request.trainAt.toRequestBody(null)

            // Assuming audio/mp3 or audio/wav or similar. "audio/*" is generic.
            val requestFile = request.audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("file", request.audioFile.name, requestFile)

            val response = voiceApi.createVoice(namePart, audioPart, f0MethodPart, epochsNumberPart, userIdPart, trainAtPart)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}