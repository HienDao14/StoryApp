package com.hiendao.presentation.voice.state

import androidx.compose.runtime.State
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.hiendao.coreui.utils.TernaryState
import com.hiendao.data.local.entity.ChapterWithContext
import com.hiendao.domain.model.Book
import com.hiendao.presentation.reader.domain.ReadingChapterPosStats
import com.hiendao.presentation.reader.domain.ReaderItem
import com.hiendao.presentation.reader.domain.chapterReadPercentage
import com.hiendao.presentation.reader.features.TextSynthesis
import com.hiendao.presentation.reader.ui.ReaderScreenState

internal data class VoiceReaderScreenState(
    val book: State<BookState>,
    val error: MutableState<String>,
    val selectedChaptersUrl: SnapshotStateMap<String, Unit>,
    val chapters: SnapshotStateList<ChapterWithContext>,
    val settingChapterSort: MutableState<TernaryState>,
    val readerState: ReaderScreenState?,
    val speakerStats: ReadingChapterPosStats? = null,
    val currentTextPlaying: TextSynthesis? = null,
    val isSpeaking: Boolean? = null,
    val isInitializing: MutableState<Boolean> = mutableStateOf(false)
) {

    val isInSelectionMode = derivedStateOf { selectedChaptersUrl.size != 0 }

    /**
     * Progress information for audio playback
     */
    data class AudioProgress(
        val chapterIndex: Int,
        val chapterCount: Int,
        val chapterItemPosition: Int,
        val chapterItemsCount: Int,
        val chapterTitle: String,
        val progressPercentage: Float,
        val currentText: String?
    ) {
        companion object {
            fun from(stats: ReadingChapterPosStats?, currentText: String?): AudioProgress? {
                return stats?.let {
                    AudioProgress(
                        chapterIndex = it.chapterIndex,
                        chapterCount = it.chapterCount,
                        chapterItemPosition = it.chapterItemPosition,
                        chapterItemsCount = it.chapterItemsCount,
                        chapterTitle = it.chapterTitle,
                        progressPercentage = it.chapterReadPercentage(),
                        currentText = currentText
                    )
                }
            }
        }
    }

    val audioProgress = AudioProgress.from(
        stats = speakerStats,
        currentText = currentTextPlaying?.itemPos?.let { itemPos ->
            when (itemPos) {
                is ReaderItem.Text -> {
                    val text = itemPos.textToDisplay
                    text.take(50) + if (text.length > 50) "..." else ""
                }
                else -> null
            }
        }
    )

    data class BookState(
        val title: String,
        val url: String,
        val completed: Boolean = false,
        val lastReadChapter: String? = null,
        val inLibrary: Boolean = false,
        val coverImageUrl: String? = null,
        val description: String = "",
        val author: String = "",
        val isFavourite: Boolean = false
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
            isFavourite = book.isFavourite
        )
    }
}