package com.hiendao.domain.model

import java.io.File

data class CreateVoiceRequest(
    val name: String,
    val audioFile: File
)
