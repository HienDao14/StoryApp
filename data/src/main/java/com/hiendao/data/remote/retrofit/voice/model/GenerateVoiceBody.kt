package com.hiendao.data.remote.retrofit.voice.model

data class GenerateVoiceBody(
    val text: String,
    val voiceId: String,
    val language: String
)
