package com.hiendao.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChapterEntity(
    val title: String,
    @PrimaryKey val id: String,
    val bookId: String,
    val position: Int,
    val read: Boolean = false,
    val lastReadPosition: Int = 0,
    val lastReadOffset: Int = 0
)