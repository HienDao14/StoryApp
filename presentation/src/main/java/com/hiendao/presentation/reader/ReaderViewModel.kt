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
import com.hiendao.presentation.reader.features.AiVoicePlayer
import com.hiendao.presentation.reader.manager.ReaderManager
import com.hiendao.presentation.reader.ui.ReaderScreenState
import com.hiendao.presentation.reader.domain.indexOfReaderItem
import com.hiendao.presentation.voice.ReadingVoiceRepository
import kotlinx.coroutines.launch
import my.noveldokusha.features.reader.ui.ReaderViewHandlersActions
import com.hiendao.coreui.appPreferences.VoicePredefineState
import com.hiendao.domain.utils.Response
import com.hiendao.presentation.reader.domain.ReaderItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import javax.inject.Inject
import kotlin.properties.Delegates
import kotlin.text.replace
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.collect
import com.hiendao.presentation.reader.features.TextSynthesis
import com.hiendao.domain.text_to_speech.Utterance


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
    private val readingVoiceRepository: ReadingVoiceRepository,
    readerViewHandlersActions: ReaderViewHandlersActions,
    @ApplicationContext private val context: Context,
    private val aiVoicePlayer: AiVoicePlayer
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
    private val wrappedTtsState = originalTtsState.copy(
        setPlaying = { isPlaying ->
            if (originalTtsState.activeAiVoice.value != null) {
                if (isPlaying) resumeAiVoice() else pauseAiVoice()
            } else {
                originalTtsState.setPlaying(isPlaying)
            }
        },
        playNextItem = {
             if (originalTtsState.activeAiVoice.value != null) nextAiVoice() else originalTtsState.playNextItem()
        },
        playPreviousItem = {
             if (originalTtsState.activeAiVoice.value != null) previousAiVoice() else originalTtsState.playPreviousItem()
        },
        playNextChapter = {
             if (originalTtsState.activeAiVoice.value != null) nextChapterAiVoice() else originalTtsState.playNextChapter()
        },
        playPreviousChapter = {
             if (originalTtsState.activeAiVoice.value != null) previousChapterAiVoice() else originalTtsState.playPreviousChapter()
        }
    )

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
            aiVoicePlayer.isPlaying.collect { playing ->
                if (originalTtsState.activeAiVoice.value != null) {
                    originalTtsState.isPlaying.value = playing
                }
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

    fun onCloseManually() {
        readerManager.close()
    }


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

    private val audioJobs = mutableMapOf<Int, Deferred<String?>>() // Map itemIndex -> Job returning audioUrl

    fun selectModelVoice(voice: VoicePredefineState) {
        // Stop any previous playback immediately
        aiVoicePlayer.stop()
        audioJobs.clear() // Clear any pending fetch jobs
        readerSession.readerTextToSpeech.stop()

        state.settings.textToSpeech.activeAiVoice.value = voice
        viewModelScope.launch {
            playAiVoiceForCurrentItem()
        }
    }

    private fun playAiVoiceForCurrentItem() {
        viewModelScope.launch {
            val currentVoice = state.settings.textToSpeech.activeAiVoice.value ?: return@launch
            val currentItemState = readerSession.readerTextToSpeech.state.currentActiveItemState.value
            
            // Assume we use the same position logic as TTS
            val chapterIndex = currentItemState.itemPos.chapterIndex
            val chapterItemPosition = currentItemState.itemPos.chapterItemPosition

            val itemIndex = indexOfReaderItem(
                list = items,
                chapterIndex = chapterIndex,
                chapterItemPosition = chapterItemPosition
            )
            
            if (itemIndex == -1) return@launch

            playAiVoiceAtIndex(itemIndex, currentVoice.modelId ?: "")
        }
    }

    private fun findNextValidItemIndex(startIndex: Int): Int {
        var index = startIndex
        while (index < items.size) {
            val item = items.getOrNull(index)
            if (item != null && getTextToSpeak(item).isNotEmpty()) {
                return index
            }
            index++
        }
        return -1
    }

    private suspend fun fetchAudioUrl(index: Int, modelId: String): String? {
        val item = items.getOrNull(index) ?: return null
        val textToSpeak = getTextToSpeak(item)
        
        if (textToSpeak.isNotEmpty()) {
            val response = readingVoiceRepository.getVoiceStory(modelId, textToSpeak)
            if (response is Response.Success) {
                val url = response.data.audio_path
                // Replace localized URL if needed
                return url.replace("http://localhost:9000", "https://ctd37qdd-9000.asse.devtunnels.ms").replace("http://127.0.0.1:9000", "https://ctd37qdd-9000.asse.devtunnels.ms")
            }
        }
        return null
    }

    private fun playAiVoiceAtIndex(index: Int, modelId: String) {
        viewModelScope.launch {
            val validIndex = findNextValidItemIndex(index)
            if (validIndex == -1) {
                state.showVoiceLoadingDialog.value = false
                return@launch
            }

            val item = items[validIndex]

            // Update UI position
            if (item is ReaderItem.Position) {
                readerSession.readerTextToSpeech.manager.setCurrentSpeakState(
                    com.hiendao.presentation.reader.features.TextSynthesis(
                        itemPos = item,
                        playState = com.hiendao.domain.text_to_speech.Utterance.PlayState.PLAYING
                    )
                )
            }

            // Get audio URL (wait for existing job or start new one)
            state.showVoiceLoadingDialog.value = true
            val urlDeferred = audioJobs.getOrPut(validIndex) {
                 async { fetchAudioUrl(validIndex, modelId) }
            }
            
            val url = urlDeferred.await()
            state.showVoiceLoadingDialog.value = false
            
            // Clean up job map to avoid memory leaks, but strictly we might simply keep it if we want to support replay without refetch
            // For now, removing it after play ensures we don't hold onto it forever, but if we want to replay we'd need to re-fetch.
            // Given the queue nature, removing it is likely safe.
            audioJobs.remove(validIndex)

            if (url != null) {
                aiVoicePlayer.play(url, onCompletion = {
                    playAiVoiceAtIndex(validIndex + 1, modelId)
                })
                // Prefetch next next
                prefetchAiVoice(validIndex + 1, modelId)
            } else {
                 // Skip failed/empty item
                 playAiVoiceAtIndex(validIndex + 1, modelId)
            }
        }
    }

    private fun prefetchAiVoice(index: Int, modelId: String) {
        viewModelScope.launch {
             val validIndex = findNextValidItemIndex(index)
             if (validIndex == -1) return@launch

             if (audioJobs.containsKey(validIndex)) return@launch

             val job = async { fetchAudioUrl(validIndex, modelId) }
             audioJobs[validIndex] = job
        }
    }

    private fun getTextToSpeak(item: ReaderItem): String {
         val text = if (item is ReaderItem.Body && item.isHtml) {
                androidx.core.text.HtmlCompat.fromHtml(
                    item.textToDisplay,
                    androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
                ).toString()
            } else if (item is ReaderItem.Text) {
                item.textToDisplay
            } else {
                ""
            }
         // Filter out text with no alphanumeric characters
         return if (text.any { it.isLetterOrDigit() }) text else ""
    }

    private fun pauseAiVoice() {
        aiVoicePlayer.pause()
        originalTtsState.isPlaying.value = false
    }

    private fun resumeAiVoice() {
        val resumed = aiVoicePlayer.resume()
        if (resumed) {
            originalTtsState.isPlaying.value = true
        } else {
             playAiVoiceForCurrentItem()
        }
    }

    private fun nextAiVoice() {
        viewModelScope.launch {
            val currentIndex = getCurrentItemIndex()
            if (currentIndex == -1) return@launch
            
            // Find next valid index
            // Start from currentIndex + 1
            var nextIndex = currentIndex + 1
            while (nextIndex < items.size) {
                 // Check if it's a position item (Title or Body) and has text
                 val item = items[nextIndex]
                 if (item is ReaderItem.Position && getTextToSpeak(item).isNotEmpty()) {
                      aiVoicePlayer.stop()
                      playAiVoiceAtIndex(nextIndex, originalTtsState.activeAiVoice.value?.modelId ?: "")
                      return@launch
                 }
                 nextIndex++
            }
        }
    }

    private fun previousAiVoice() {
        viewModelScope.launch {
            val currentIndex = getCurrentItemIndex()
            if (currentIndex <= 0) return@launch
            
            // Find prev valid index
             var prevIndex = currentIndex - 1
            while (prevIndex >= 0) {
                 val item = items[prevIndex]
                 if (item is ReaderItem.Position && getTextToSpeak(item).isNotEmpty()) {
                      aiVoicePlayer.stop()
                      playAiVoiceAtIndex(prevIndex, originalTtsState.activeAiVoice.value?.modelId ?: "")
                      return@launch
                 }
                 prevIndex--
            }
        }
    }

    private fun nextChapterAiVoice() {
        viewModelScope.launch {
            val currentItemState = originalTtsState.currentActiveItemState.value
            val nextChapterIndex = currentItemState.itemPos.chapterIndex + 1

            aiVoicePlayer.stop()
            originalTtsState.isPlaying.value = false

            if (!chaptersLoader.isChapterIndexValid(nextChapterIndex)) {
                return@launch
            }

            if (!chaptersLoader.isChapterIndexLoaded(nextChapterIndex)) {
                originalTtsState.isLoadingChapter.value = true
                chaptersLoader.tryLoadNext() // Ensure this matches ReaderChaptersLoader API
                chaptersLoader.chapterLoadedFlow.filter { it.chapterIndex == nextChapterIndex }.take(1).collect()
                originalTtsState.isLoadingChapter.value = false
            }

            // Find start index of new chapter
            val startIndex = indexOfReaderItem(items, nextChapterIndex, 0)
            if (startIndex != -1) {
                playAiVoiceAtIndex(startIndex, originalTtsState.activeAiVoice.value?.modelId ?: "")
            }
        }
    }

    private fun previousChapterAiVoice() {
        viewModelScope.launch {
            val currentItemState = originalTtsState.currentActiveItemState.value
            val currentChapterIndex = currentItemState.itemPos.chapterIndex
            
            val targetChapterIndex = if (currentItemState.itemPos is ReaderItem.Title) {
                currentChapterIndex - 1
            } else {
                currentChapterIndex
            }

            aiVoicePlayer.stop()
            originalTtsState.isPlaying.value = false

            if (!chaptersLoader.isChapterIndexValid(targetChapterIndex)) {
                return@launch
            }

            if (!chaptersLoader.isChapterIndexLoaded(targetChapterIndex)) {
                originalTtsState.isLoadingChapter.value = true
                chaptersLoader.tryLoadPrevious()
                chaptersLoader.chapterLoadedFlow.filter { it.chapterIndex == targetChapterIndex }.take(1).collect()
                originalTtsState.isLoadingChapter.value = false
            }

            // Find start index of target chapter
            val startIndex = indexOfReaderItem(items, targetChapterIndex, 0)
            if (startIndex != -1) {
                playAiVoiceAtIndex(startIndex, originalTtsState.activeAiVoice.value?.modelId ?: "")
            }
        }
    }

    private suspend fun getCurrentItemIndex(): Int {
        val currentItemPos = originalTtsState.currentActiveItemState.value.itemPos
        return indexOfReaderItem(
            list = items,
            chapterIndex = currentItemPos.chapterIndex,
            chapterItemPosition = currentItemPos.chapterItemPosition
        )
    }

    override fun onCleared() {
        super.onCleared()
        aiVoicePlayer.release()
    }
}
