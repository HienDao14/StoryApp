package com.hiendao.domain.model

import java.io.File

data class CreateVoiceRequest(
    val name: String,
    val audioFile: File,
    val f0Method: String = "crepe",
    val epochsNumber: String = "10",
    val userId: String,
    val trainAt: String
)
