package com.hiendao.domain.model


sealed interface ReaderItem {
    val id: String
    val chapterId: String
    val order: Int
}

data class ReaderTextItem(
    override val id: String,
    override val chapterId: String,
    override val order: Int,
    val text: String,
    val type: TextType,
    val location: Location = Location.MIDDLE
) : ReaderItem

data class ReaderImageItem(
    override val id: String,
    override val chapterId: String,
    override val order: Int,
    val imageUrl: String,
    val caption: String? = null,
    val location: Location = Location.MIDDLE
) : ReaderItem

enum class TextType {
    TITLE,
    BODY,
    QUOTE,
    AUTHOR_NOTE
}

enum class Location {
    FIRST,
    MIDDLE,
    LAST
}
