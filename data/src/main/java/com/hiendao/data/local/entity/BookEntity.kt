package com.hiendao.data.local.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class BookEntity(
    val title: String,
    @PrimaryKey val id: String,
    val completed: Boolean = false,
    val lastReadChapter: String? = null,
    val inLibrary: Boolean = false,
    val coverImageUrl: String = "",
    val description: String = "",
    val lastReadEpochTimeMilli: Long = 0,
    var isFavourite: Boolean = false,
    var author: String = "",
    var ageRating: String? = null,
    var categories: String? = null
) : Parcelable