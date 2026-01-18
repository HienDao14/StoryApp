package com.hiendao.presentation.player

import android.content.Context
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiendao.presentation.reader.domain.chapterReadPercentage
import com.hiendao.presentation.reader.manager.ReaderManager
import com.hiendao.presentation.voice.manager.AiNarratorManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GlobalPlayerState(
    val isVisible: Boolean = false,
    val isPlaying: Boolean = false,
    val bookTitle: String = "",
    val chapterTitle: String = "",
    val coverUrl: String? = null,
    val bookUrl: String = "",
    val chapterUrl: String = "",
    val progress: Float = 0f,
    val isLoading: Boolean = false
)

@HiltViewModel
internal class GlobalPlayerViewModel @Inject constructor(
    private val readerManager: ReaderManager,
    private val aiNarratorManager: AiNarratorManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(GlobalPlayerState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            readerManager.sessionFlow.collectLatest { session ->
                if (session == null) {
                    _state.update { it.copy(isVisible = false, isPlaying = false) }
                } else {
                     // Observe session details using flow from user interaction and player state
                     combine(
                         snapshotFlow { session.readerTextToSpeech.state.isPlaying.value },
                         snapshotFlow { session.readerTextToSpeech.currentTextPlaying.value },
                         snapshotFlow { session.readingStats.value },
                         aiNarratorManager.isLoading
                     ) { isPlaying, currentText, stats, isLoading ->
                         Triple(isPlaying, stats, isLoading)
                     }.collect { (isPlaying, stats, isLoading) ->
                         _state.update { current ->
                             current.copy(
                                 isVisible = if (isPlaying || isLoading) true else current.isVisible,
                                 isPlaying = isPlaying, // Is true whenever TTS or AI is playing if hooked up correctly
                                 bookTitle = session.bookTitle ?: "",
                                 chapterTitle = stats?.chapterTitle ?: "",
                                 coverUrl = session.bookCoverUrl ?: "",
                                 bookUrl = session.bookUrl,
                                 chapterUrl = session.currentChapter.chapterUrl,
                                 progress = stats?.chapterReadPercentage() ?: 0f,
                                 isLoading = isLoading
                             )
                         }
                     }
                }
            }
        }
    }

    fun hidePlayer() {
        _state.update { it.copy(isVisible = false) }
    }

    fun play() {
        if (aiNarratorManager.activeVoice.value != null) {
            aiNarratorManager.resume()
             com.hiendao.presentation.reader.services.NarratorMediaControlsService.start(context)
        } else {
            readerManager.session?.readerTextToSpeech?.state?.setPlaying?.invoke(true)
        }
    }

    fun pause() {
        if (aiNarratorManager.activeVoice.value != null) {
            aiNarratorManager.pause()
        } else {
            readerManager.session?.readerTextToSpeech?.state?.setPlaying?.invoke(false)
        }
    }

    fun next() {
        if (aiNarratorManager.activeVoice.value != null) {
            aiNarratorManager.next()
        } else {
             readerManager.session?.readerTextToSpeech?.state?.playNextItem?.invoke()
        }
    }
    
    fun prev() {
        if (aiNarratorManager.activeVoice.value != null) {
            aiNarratorManager.previous()
        } else {
             readerManager.session?.readerTextToSpeech?.state?.playPreviousItem?.invoke()
        }
    }
    
    fun close() {
        readerManager.close()
        aiNarratorManager.stop()
         com.hiendao.presentation.reader.services.NarratorMediaControlsService.stop(context)
    }
}
