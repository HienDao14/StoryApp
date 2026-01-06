package com.hiendao.presentation.voice

import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.coreui.appPreferences.VoicePredefineState
import com.hiendao.data.remote.retrofit.voice.VoiceApi
import com.hiendao.data.remote.retrofit.voice.model.GenerateVoiceBody
import com.hiendao.data.remote.retrofit.voice.model.VoiceResponse
import com.hiendao.data.remote.retrofit.voice.model.VoiceResponseItem
import com.hiendao.data.remote.retrofit.voice.model.VoiceStoryResponse
import com.hiendao.domain.utils.Response
import javax.inject.Inject

class ReadingVoiceRepository @Inject constructor(
    private val voiceApi: VoiceApi,
    private val appPreferences: AppPreferences
) {

    suspend fun getAllVoices(): Response<Boolean> {
        return try {
            val response = voiceApi.getMyVoices()

            val voiceSaved = response.map { it.toVoicePredefineState() }
            val setVoiceSaved = mutableSetOf<VoicePredefineState>()
            setVoiceSaved.addAll(voiceSaved)
            val currentList = appPreferences.READER_TEXT_TO_SPEECH_SAVED_PREDEFINED_LIST.value
            setVoiceSaved.addAll(currentList)
            appPreferences.READER_TEXT_TO_SPEECH_SAVED_PREDEFINED_LIST.value= setVoiceSaved.toList()
            if(setVoiceSaved.size > currentList.size){
                Response.Success(true)
            } else Response.None
        } catch (e : Exception){
            Response.Error(e.message ?: "Unknown error", e)
        }
    }

    suspend fun getVoiceStory(modelId: String, text: String): Response<VoiceStoryResponse> {
        return try {
            val response = voiceApi.getVoiceStory(GenerateVoiceBody(text, modelId))
            Response.Success(response)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error", e)
        }
    }

    fun VoiceResponseItem.toVoicePredefineState(): VoicePredefineState {
        return VoicePredefineState(
            savedName = this.name,
            voiceId = this.id,
            pitch = 1f,
            speed = 1f,
            modelPath = this.modelId,
            modelId = this.modelId
        )
    }
}