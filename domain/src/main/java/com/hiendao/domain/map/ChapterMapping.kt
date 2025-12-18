package com.hiendao.domain.map

import com.hiendao.data.local.entity.ChapterEntity
import com.hiendao.domain.model.Chapter

fun ChapterEntity.toDomain(): Chapter {
    return Chapter(
        id = this.id,
        bookId = this.bookId,
        title = this.title,
        content = "",                      // Entity không chứa nội dung chương
        order = this.position,
        isRead = this.read,
        readProgress = if (read) 1f else 0f,  // Hoặc bạn tùy chỉnh
        lastReadPosition = this.lastReadPosition,
        lastReadOffset = this.lastReadOffset,
        wordCount = 0,                       // Không có trong entity
        createdAt = System.currentTimeMillis(), // Tùy bạn muốn lấy từ đâu
        updatedAt = System.currentTimeMillis()
    )
}

fun Chapter.toEntity(): ChapterEntity {
    return ChapterEntity(
        id = this.id,
        title = this.title,
        bookId = this.bookId,
        position = this.order,
        read = this.isRead,
        lastReadPosition = this.lastReadPosition,
        lastReadOffset = this.lastReadOffset
    )
}