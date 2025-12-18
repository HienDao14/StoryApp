package com.hiendao.presentation.reader.domain

import com.hiendao.domain.model.Chapter


internal data class ChapterStats(
    val itemsCount: Int,
    val chapter: Chapter,
    val orderedChaptersIndex: Int
)