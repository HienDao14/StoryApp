package com.hiendao.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val url: String = "",
    val coverImageUrl: String = "",
    val description: String = "",
    val totalChapters: Int = 0,
    val lastReadChapter: String? = null,
    val lastReadPosition: Int = 0,
    val lastReadOffset: Int = 0,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    var isFavourite: Boolean = false,
    var isDownloaded: Boolean = false,
    val lastReadEpochTimeMilli: Long = 0,
    var inLibrary: Boolean = false,
    val ageRating: Int?=null,
    val categories: List<String>?=null
) : Parcelable