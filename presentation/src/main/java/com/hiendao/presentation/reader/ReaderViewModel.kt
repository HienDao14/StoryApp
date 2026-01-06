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
import android.media.MediaPlayer
import com.hiendao.coreui.appPreferences.VoicePredefineState
import com.hiendao.domain.utils.Response
import com.hiendao.presentation.reader.domain.ReaderItem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.properties.Delegates
import kotlin.text.replace

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
            textToSpeech = readerSession.readerTextToSpeech.state,
            liveTranslation = readerSession.readerLiveTranslation.state,
            fullScreen = appPreferences.READER_FULL_SCREEN.state(viewModelScope),
            style = ReaderScreenState.Settings.StyleSettingsData(
                followSystem = appPreferences.THEME_FOLLOW_SYSTEM.state(viewModelScope),
                currentTheme = derivedStateOf { themeId.value.toTheme },
                textFont = appPreferences.READER_FONT_FAMILY.state(viewModelScope),
                textSize = appPreferences.READER_FONT_SIZE.state(viewModelScope),
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

    private val aiVoicePlayer = AiVoicePlayer(context)
    private val audioQueue = mutableMapOf<Int, String>() // Map itemIndex -> audioUrl

    fun selectModelVoice(voice: VoicePredefineState) {
        state.settings.textToSpeech.activeAiVoice.value = voice
        viewModelScope.launch {
            // Stop local TTS if any
             if (readerSession.readerTextToSpeech.isSpeaking.value) {
                readerSession.readerTextToSpeech.stop()
            }
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

            // Check queue
            val queuedUrl = audioQueue[validIndex]
            if (queuedUrl != null) {
                audioQueue.remove(validIndex)
                aiVoicePlayer.play(queuedUrl, onCompletion = {
                    playAiVoiceAtIndex(validIndex + 1, modelId)
                })
                // Prefetch next next
                prefetchAiVoice(validIndex + 1, modelId)
                return@launch
            }

            // Fetch
            state.showVoiceLoadingDialog.value = true
            val textToSpeak = getTextToSpeak(item)

            if (textToSpeak.isNotEmpty()) {
                val response = readingVoiceRepository.getVoiceStory(modelId, textToSpeak)
                state.showVoiceLoadingDialog.value = false
                if (response is Response.Success) {
                    val url = response.data.audio_path
                    // Replace localized URL if needed, similar to previous implementation
                    val finalUrl = url.replace("http://localhost:9000", "https://ctd37qdd-9000.asse.devtunnels.ms").replace("http://127.0.0.1:9000", "https://ctd37qdd-9000.asse.devtunnels.ms")

                    aiVoicePlayer.play(finalUrl, onCompletion = {
                        playAiVoiceAtIndex(validIndex + 1, modelId)
                    })
                    // Prefetch next
                    prefetchAiVoice(validIndex + 1, modelId)
                }
            } else {
                state.showVoiceLoadingDialog.value = false
                // Should theoretically be unreachable due to findNextValidItemIndex check, but good for safety
                playAiVoiceAtIndex(validIndex + 1, modelId)
            }
        }
    }

    private fun prefetchAiVoice(index: Int, modelId: String) {
        viewModelScope.launch {
            val validIndex = findNextValidItemIndex(index)
            if (validIndex == -1) return@launch

            if (audioQueue.containsKey(validIndex)) return@launch

            val item = items.getOrNull(validIndex) ?: return@launch
            val textToSpeak = getTextToSpeak(item)
            if (textToSpeak.isNotEmpty()) {
                val response = readingVoiceRepository.getVoiceStory(modelId, textToSpeak)
                if (response is Response.Success) {
                    val url = response.data.audio_path
                    // Replace localized URL if needed
                    val finalUrl = url.replace("http://localhost:9000", "https://ctd37qdd-9000.asse.devtunnels.ms").replace("http://127.0.0.1:9000", "https://ctd37qdd-9000.asse.devtunnels.ms")
                    audioQueue[validIndex] = finalUrl
                }
            }
        }
    }

    private fun getTextToSpeak(item: ReaderItem): String {
         return if (item is ReaderItem.Body && item.isHtml) {
                androidx.core.text.HtmlCompat.fromHtml(
                    item.textToDisplay,
                    androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
                ).toString()
            } else if (item is ReaderItem.Text) {
                item.textToDisplay
            } else {
                ""
            }
    }

    override fun onCleared() {
        super.onCleared()
        aiVoicePlayer.release()
    }
}
