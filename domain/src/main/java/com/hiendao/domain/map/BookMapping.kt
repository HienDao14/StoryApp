package com.hiendao.domain.map

import com.hiendao.data.local.entity.BookEntity
import com.hiendao.data.remote.retrofit.book.model.BookDTO
import com.hiendao.data.remote.retrofit.book.model.BookResponseDTO
import com.hiendao.data.remote.retrofit.book.model.Content
import com.hiendao.data.utils.Constants
import com.hiendao.data.utils.toMillisLegacy
import com.hiendao.domain.model.Book

fun Content.toDomain(): Book {
    return Book(
        id = this.id,
        title = this.title,
        author = this.author,
        url = this.id,
        coverImageUrl = this.coverImageUrl ?: "",
        completed = this.status == Constants.BookStatus.COMPLETED,
        isFavourite = isFavorite
    )
}

fun List<Content>.toDomainListFromContent(): List<Book> {
    return this.map { it.toDomain() }
}

fun BookEntity.toDomain(): Book {
    return Book(
        id = this.id,                       // Domain dùng id, entity dùng url
        title = this.title,
        author = "",                         // Entity không có
        url = this.id,
        coverImageUrl = this.coverImageUrl,
        description = this.description,
        totalChapters = 0,                   // Không có trong entity
        lastReadChapter = this.lastReadChapter,
        lastReadPosition = 0,                // Không có trong entity
        lastReadOffset = 0,                  // Không có trong entity
        completed = this.completed,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        isFavourite = isFavourite,
        isDownloaded = this.inLibrary,       // inLibrary = đã tải về
        lastReadEpochTimeMilli = this.lastReadEpochTimeMilli,
        inLibrary = inLibrary
    )
}


fun Book.toEntity(): BookEntity {
    return BookEntity(
        title = this.title,
        id = this.url,                       // Domain id = url
        completed = this.completed,
        lastReadChapter = this.lastReadChapter,
        inLibrary = this.inLibrary,        // map ngược
        coverImageUrl = this.coverImageUrl,
        description = this.description,
        lastReadEpochTimeMilli = this.lastReadEpochTimeMilli,
        isFavourite = isFavourite
    )
}

fun BookDTO.toBook(): Book {
    return Book(
        id = this.id,
        title = this.title,
        author = this.author,
        url = this.id,
        coverImageUrl = this.coverImageUrl,
        description = this.description,
        totalChapters = this.totalChapter,
        completed = this.status == "completed"
    )
}

fun List<BookDTO>.toDomainList(): List<Book> {
    return this.map { it.toBook() }
}

fun BookResponseDTO.toDomain(): Book {
    return Book(
        id = this.id ?: "",
        title = this.title ?: "",
        author = this.author ?: "",
        url = this.id ?: "",
        coverImageUrl = this.coverImageUrl ?: "",
        description = this.description ?: "",
        totalChapters = this.chapters?.size ?: 0,
        completed = this.status == Constants.BookStatus.COMPLETED,
        createdAt = this.createDate?.toMillisLegacy() ?: 0L,
        updatedAt = this.lastUpdateDate?.toMillisLegacy() ?: 0L,
        isFavourite = this.isFavorite ?: false,
        inLibrary = false
    )
}

fun BookResponseDTO.toEntity(): BookEntity {
    return BookEntity(
        id = this.id ?: "",
        title = this.title ?: "",
        coverImageUrl = this.coverImageUrl ?: "",
        description = this.description ?: "",
        completed = this.status == Constants.BookStatus.COMPLETED,
        isFavourite = this.isFavorite ?: false,
        inLibrary = false
    )
}
