package com.hiendao.presentation.bookDetail.state

import androidx.compose.runtime.State
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.hiendao.coreui.utils.TernaryState
import com.hiendao.data.local.entity.ChapterWithContext
import com.hiendao.domain.model.Book
import com.hiendao.domain.model.Category

internal data class ChaptersScreenState(
    val book: State<BookState>,
    val error: MutableState<String>,
    val selectedChaptersUrl: SnapshotStateMap<String, Unit>,
    val chapters: SnapshotStateList<ChapterWithContext>,
    val isRefreshing: MutableState<Boolean>,
    val settingChapterSort: MutableState<TernaryState>,
    val isLocalSource: State<Boolean>,
    val isRefreshable: State<Boolean>,
    var isLoading: MutableState<Boolean>
) {

    val isInSelectionMode = derivedStateOf { selectedChaptersUrl.size != 0 }

    data class BookState(
        val title: String,
        val url: String,
        val completed: Boolean = false,
        val lastReadChapter: String? = null,
        val inLibrary: Boolean = false,
        val coverImageUrl: String? = null,
        val description: String = "",
        val author: String = "",
        val isFavourite: Boolean = false,
        val ageRating: Int? = null,
        val categories: List<String> = emptyList<String>()
    ) {
        constructor(book: Book) : this(
            title = book.title,
            url = book.url,
            completed = book.completed,
            lastReadChapter = book.lastReadChapter,
            inLibrary = book.inLibrary,
            coverImageUrl = book.coverImageUrl,
            description = book.description,
            author = book.author,
            isFavourite = book.isFavourite,
            ageRating = book.ageRating,
            categories = book.categories ?: emptyList<String>()
        )
    }
}