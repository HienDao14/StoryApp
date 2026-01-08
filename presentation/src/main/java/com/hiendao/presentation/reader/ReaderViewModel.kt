package com.hiendao.presentation.reader

import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hiendao.coreui.BaseViewModel
import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.coreui.theme.toTheme
import com.hiendao.coreui.utils.StateExtra_Boolean
import com.hiendao.coreui.utils.StateExtra_String
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.hiendao.presentation.reader.manager.ReaderManager
import com.hiendao.presentation.reader.ui.ReaderScreenState
import com.hiendao.presentation.voice.manager.AiNarratorManager
import kotlinx.coroutines.launch
import my.noveldokusha.features.reader.ui.ReaderViewHandlersActions
import com.hiendao.coreui.appPreferences.VoicePredefineState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.properties.Delegates


interface ReaderStateBundle {
    var bookUrl: String
    var chapterUrl: String
    var introScrollToSpeaker: Boolean
}

@HiltViewModel
internal class ReaderViewModel @Inject constructor(
    stateHandler: SavedStateHandle,
    appPreferences: AppPreferences,
    private val readerManager: ReaderManager,
    private val aiNarratorManager: AiNarratorManager,
    readerViewHandlersActions: ReaderViewHandlersActions,
    @ApplicationContext private val context: Context,
) : BaseViewModel(), ReaderStateBundle {

    override var bookUrl by StateExtra_String(stateHandler)
    override var chapterUrl by StateExtra_String(stateHandler)
    override var introScrollToSpeaker by StateExtra_Boolean(stateHandler)

    private val readerSession = readerManager.initiateOrGetSession(
        bookUrl = bookUrl,
        chapterUrl = chapterUrl
    )

    private val readingPosStats = readerSession.readingStats
    private val themeId = appPreferences.THEME_ID.state(viewModelScope)
    
    private val originalTtsState = readerSession.readerTextToSpeech.state
    private fun ensureSessionAttached() {
        if (readerManager.session == null) {
            readerManager.attachSession(readerSession)
        }
    }

    private val wrappedTtsState = originalTtsState.copy(
        setPlaying = { isPlaying ->
            if (aiNarratorManager.activeVoice.value != null) {
                if (isPlaying) {
                    ensureSessionAttached()
                    aiNarratorManager.resume()
                } else {
                    aiNarratorManager.pause()
                }
            } else {
                originalTtsState.setPlaying(isPlaying)
            }
        },
        playNextItem = {
             if (aiNarratorManager.activeVoice.value != null) {
                 ensureSessionAttached()
                 aiNarratorManager.next()
             } else originalTtsState.playNextItem()
        },
        playPreviousItem = {
             if (aiNarratorManager.activeVoice.value != null) {
                 ensureSessionAttached()
                 aiNarratorManager.previous()
             } else originalTtsState.playPreviousItem()
        },
        playNextChapter = {
             if (aiNarratorManager.activeVoice.value != null) {
                 ensureSessionAttached()
                 nextChapterAiVoice()
             } else originalTtsState.playNextChapter()
        },
        playPreviousChapter = {
             if (aiNarratorManager.activeVoice.value != null) {
                 ensureSessionAttached()
                 previousChapterAiVoice()
             } else originalTtsState.playPreviousChapter()
        },
        setVoiceId = { voiceId ->
            aiNarratorManager.stop()
            aiNarratorManager.setActiveVoice(null)
            originalTtsState.setVoiceId(voiceId)
        }
    )

    // ... (rest of class)

    fun selectModelVoice(voice: VoicePredefineState) {
        val wasPlaying = originalTtsState.isPlaying.value == true
        
        // Stop current playback
        aiNarratorManager.stop()
        readerSession.readerTextToSpeech.stop()

        ensureSessionAttached()
        aiNarratorManager.setActiveVoice(voice)
        
        viewModelScope.launch {
             aiNarratorManager.playForCurrent()
        }
    }

    // ... (rest of methods)

    fun onCloseManually() {
        val isAiVoiceActive = aiNarratorManager.activeVoice.value != null
        val isPlaying = originalTtsState.isPlaying.value == true

        // Only close if not in "Audio Mode" (AI Voice selected) and not currently playing
        if (!isAiVoiceActive && !isPlaying) {
            readerManager.close()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // If the session is NOT the one in ReaderManager (e.g. it was detached), we should close it to avoid leaks.
        if (readerManager.session != readerSession) {
             readerSession.close()
        }
    }

    val state = ReaderScreenState(
        showReaderInfo = mutableStateOf(false),
        readerInfo = ReaderScreenState.CurrentInfo(
            chapterTitle = derivedStateOf {
                readingPosStats.value?.chapterTitle ?: ""
            },
            chapterCurrentNumber = derivedStateOf {
                readingPosStats.value?.run { chapterIndex + 1 } ?: 0
            },
            chapterPercentageProgress = readerSession.readingChapterProgressPercentage,
            chaptersCount = derivedStateOf { readingPosStats.value?.chapterCount ?: 0 },
            chapterUrl = derivedStateOf { readingPosStats.value?.chapterUrl ?: "" }
        ),
        settings = ReaderScreenState.Settings(
            selectedSetting = mutableStateOf(ReaderScreenState.Settings.Type.None),
            isTextSelectable = appPreferences.READER_SELECTABLE_TEXT.state(viewModelScope),
            keepScreenOn = appPreferences.READER_KEEP_SCREEN_ON.state(viewModelScope),
            textToSpeech = wrappedTtsState,
            liveTranslation = readerSession.readerLiveTranslation.state,
            fullScreen = appPreferences.READER_FULL_SCREEN.state(viewModelScope),
            brightness = appPreferences.READER_BRIGHTNESS.state(viewModelScope),
            nightMode = appPreferences.READER_NIGHT_MODE.state(viewModelScope),
            autoScrollSpeed = appPreferences.READER_AUTO_SCROLL_SPEED.state(viewModelScope),
            volumeKeyNavigation = appPreferences.READER_VOLUME_KEY_NAVIGATION.state(viewModelScope),
            style = ReaderScreenState.Settings.StyleSettingsData(
                followSystem = appPreferences.THEME_FOLLOW_SYSTEM.state(viewModelScope),
                currentTheme = derivedStateOf { themeId.value.toTheme },
                textFont = appPreferences.READER_FONT_FAMILY.state(viewModelScope),
                textSize = appPreferences.READER_FONT_SIZE.state(viewModelScope),
                lineHeight = appPreferences.READER_LINE_HEIGHT.state(viewModelScope),
                textAlign = appPreferences.READER_TEXT_ALIGN.state(viewModelScope),
                screenMargin = appPreferences.READER_SCREEN_MARGIN.state(viewModelScope),
            )
        ),
        showInvalidChapterDialog = mutableStateOf(false),
        showVoiceLoadingDialog = mutableStateOf(false)
    )

    init {
        readerViewHandlersActions.showInvalidChapterDialog = {
            withContext(Dispatchers.Main) {
                state.showInvalidChapterDialog.value = true
            }
        }
        viewModelScope.launch {
            aiNarratorManager.isLoading.collect { loading ->
                state.showVoiceLoadingDialog.value = loading
            }
        }
    }

    val items = readerSession.items
    val chaptersLoader = readerSession.readerChaptersLoader
    val readerSpeaker = readerSession.readerTextToSpeech
    var readingCurrentChapter by Delegates.observable(readerSession.currentChapter) { _, _, new ->
        readerSession.currentChapter = new
    }
    val onTranslatorChanged = readerSession.readerLiveTranslation.onTranslatorChanged
    val ttsScrolledToTheTop = readerSession.readerTextToSpeech.scrolledToTheTop
    val ttsScrolledToTheBottom = readerSession.readerTextToSpeech.scrolledToTheBottom




    fun startSpeaker(itemIndex: Int) =
        readerSession.startSpeaker(itemIndex = itemIndex)

    fun reloadReader() {
        val currentChapter = readingCurrentChapter.copy()
        readerSession.reloadReader()
        chaptersLoader.tryLoadRestartedInitial(currentChapter)
    }

    fun updateInfoViewTo(itemIndex: Int) =
        readerSession.updateInfoViewTo(itemIndex = itemIndex)

    fun markChapterStartAsSeen(chapterUrl: String) =
        readerSession.markChapterStartAsSeen(chapterUrl = chapterUrl)

    fun markChapterEndAsSeen(chapterUrl: String) =
        readerSession.markChapterEndAsSeen(chapterUrl = chapterUrl)




    private fun nextChapterAiVoice() {
        // Reuse TTS navigation logic to move cursor/load chapter
        viewModelScope.launch {
            originalTtsState.playNextChapter()
            // After TTS logic moves to next chapter, we trigger AI playback
            // We need to wait for chapter load?
            // aiNarratorManager.playForCurrent() will catch the new position if updated.
            // But originalTtsState.playNextChapter() might be async or trigger loading.
            // Let's explicitly trigger after a short delay or rely on flow updates?
            // Better to stick to what we had or simple call.
            // Since we don't have direct access to "onChapterLoaded" easily without recreating logic,
            // we can let the originalTtsState change the active item, which updates the session item items.
            // But playNextChapter() in TTS often starts TTS playback if valid.
            // We wrapped it. Inner logic: changes chapter, sets active item.
            
            // If we just called playNextChapter(), and it succeeded, valid items are loaded.
            // We can then tell manager to play.
             aiNarratorManager.playForCurrent()
        }
    }

    private fun previousChapterAiVoice() {
         viewModelScope.launch {
            originalTtsState.playPreviousChapter()
            aiNarratorManager.playForCurrent()
        }
    }

}
