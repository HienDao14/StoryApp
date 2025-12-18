package com.hiendao.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChapterBodyEntity(
    @PrimaryKey val chapterId: String,
    val body: String
)