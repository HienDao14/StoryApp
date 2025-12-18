package com.hiendao.domain.model

data class Chapter(
    val id: String,
    val bookId: String,
    val title: String,
    val content: String,
    val order: Int,
    val isRead: Boolean = false,
    val readProgress: Float = 0f, // 0.0 to 1.0
    var lastReadPosition: Int = 0,
    val lastReadOffset: Int = 0,
    val wordCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
