package com.hiendao.data.remote.retrofit.story.model

data class CreateStoryRequest(
    val title: String,
    val freeText: String,
    val durationSeconds: Int,
    val readingLevel: String,
    val genre: String,
    val characters: List<Character>,
    val tone: String,
    val keyMessages: List<String>,
    val includeSoundCues: Boolean,
    val language: String,
    val additionalInstructions: String
) {
    data class Character(
        val name: String,
        val shortDescription: String
    )
}