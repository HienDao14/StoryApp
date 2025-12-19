package com.hiendao.data.remote.retrofit.voice.model

data class VoiceResponseItem(
    val createDate: String,
    val createdBy: String,
    val id: String,
    val lastUpdateDate: String,
    val lastUpdatedBy: String,
    val modelId: String,
    val modelPath: String,
    val name: String,
    val webView: Boolean
)