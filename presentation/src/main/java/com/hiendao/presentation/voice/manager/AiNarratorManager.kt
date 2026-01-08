package com.hiendao.presentation.voice.manager

import com.hiendao.coreui.appPreferences.VoicePredefineState
import com.hiendao.coreui.utils.Toasty
import com.hiendao.data.utils.AppCoroutineScope
import com.hiendao.domain.text_to_speech.Utterance
import com.hiendao.domain.utils.Response
import com.hiendao.presentation.reader.domain.ReaderItem
import com.hiendao.presentation.reader.domain.indexOfReaderItem
import com.hiendao.presentation.reader.features.AiVoicePlayer
import com.hiendao.presentation.reader.features.TextSynthesis
import com.hiendao.presentation.reader.manager.ReaderManager
import com.hiendao.presentation.voice.ReadingVoiceRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AiNarratorManager @Inject constructor(
    private val readerManager: ReaderManager,
    private val readingVoiceRepository: ReadingVoiceRepository,
    private val aiVoicePlayer: AiVoicePlayer,
    private val appScope: AppCoroutineScope,
    private val appPreferences: com.hiendao.coreui.appPreferences.AppPreferences
) {

    private val _activeVoice = MutableStateFlow<VoicePredefineState?>(null)
    val activeVoice = _activeVoice.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val audioJobs = mutableMapOf<Int, Deferred<String?>>()
    private val urlCache = mutableMapOf<String, String>()

    // Observe AI player state to update session state if needed
    init {
        appScope.launch {
            aiVoicePlayer.isPlaying.collect { playing ->
                val session = readerManager.session
                if (session != null && _activeVoice.value != null) {
                    session.readerTextToSpeech.state.isPlaying.value = playing
                }
            }
        }
    }

    fun setActiveVoice(voice: VoicePredefineState?) {
        _activeVoice.value = voice
        // Update session's active AI voice for UI binding
        readerManager.session?.readerTextToSpeech?.state?.activeAiVoice?.value = voice
        
        // Save to preferences
        if (voice != null) {
            appPreferences.LAST_SELECTED_VOICE.value = voice
        }
    }

    fun stop() {
        aiVoicePlayer.stop()
        audioJobs.clear()
        readerManager.session?.readerTextToSpeech?.state?.isPlaying?.value = false
        _isLoading.value = false
    }

    fun pause() {
        aiVoicePlayer.pause()
        readerManager.session?.readerTextToSpeech?.state?.isPlaying?.value = false
    }

    fun resume() {
        if (_activeVoice.value == null) {
            // Restore if needed for resume? Potentially.
            val lastVoice = appPreferences.LAST_SELECTED_VOICE.value
            if (lastVoice != null) {
                setActiveVoice(lastVoice)
            } else {
                 return
            }
        }
        val resumed = aiVoicePlayer.resume()
        if (resumed) {
            readerManager.session?.readerTextToSpeech?.state?.isPlaying?.value = true
        } else {
             // If cannot resume (maybe stopped), play current
            playForCurrent()
        }
    }

    fun playForCurrent() {
        val session = readerManager.session ?: return
        
        // Restore last voice if null
        if (_activeVoice.value == null) {
             val lastVoice = appPreferences.LAST_SELECTED_VOICE.value
             if (lastVoice != null) {
                 setActiveVoice(lastVoice)
             } else {
                 // Fallback or just return? If no voice selected ever, user needs to select one.
                 // Ideally UI should prompt or default, but for now we return.
                 return
             }
        }

        val currentVoice = _activeVoice.value!!

        appScope.launch {
            val currentItemPos = session.readerTextToSpeech.state.currentActiveItemState.value.itemPos
            
            val itemIndex = indexOfReaderItem(
                list = session.items,
                chapterIndex = currentItemPos.chapterIndex,
                chapterItemPosition = currentItemPos.chapterItemPosition
            )
            
            if (itemIndex != -1) {
                playAtIndex(itemIndex, currentVoice.modelId ?: "")
            }
        }
    }

    private fun playAtIndex(index: Int, modelId: String) {
        appScope.launch {
            val session = readerManager.session ?: return@launch
            val items = session.items

            val validIndex = findNextValidItemIndex(items, index)
            if (validIndex == -1) {
                _isLoading.value = false
                
                // Check if End of Story
                val stats = session.readingStats.value
                val currentChapterIndex = session.readerTextToSpeech.state.currentActiveItemState.value.itemPos.chapterIndex
                
                if (stats != null && currentChapterIndex >= stats.chapterCount - 1) {
                    stop()
                    // Detach session to hide MiniPlayer from global view, 
                    // but ReaderViewModel still holds the session if active.
                    readerManager.detachSession()
                }
                
                return@launch
            }

            val item = items[validIndex]

            // Update UI position
            if (item is ReaderItem.Position) {
                session.readerTextToSpeech.manager.setCurrentSpeakState(
                    TextSynthesis(
                        itemPos = item,
                        playState = Utterance.PlayState.PLAYING
                    )
                )
            }

            _isLoading.value = true
            
            val urlDeferred = audioJobs.getOrPut(validIndex) {
                 async { fetchAudioUrl(items, validIndex, modelId) }
            }
            
            val url = urlDeferred.await()
            // _isLoading.value = false // Removed: Wait for player prepare

            audioJobs.remove(validIndex)

            if (url != null) {
                aiVoicePlayer.play(
                    url = url, 
                    onCompletion = {
                        playAtIndex(validIndex + 1, modelId)
                    },
                    onPrepared = {
                         _isLoading.value = false
                    },
                    onError = {
                        _isLoading.value = false
                    }
                )
                prefetch(items, validIndex + 1, modelId)
            } else {
                 _isLoading.value = false // If no URL found (error case), stop loading
                playAtIndex(validIndex + 1, modelId)
            }
        }
    }
    
    private fun prefetch(items: List<ReaderItem>, index: Int, modelId: String) {
        appScope.launch {
             val validIndex = findNextValidItemIndex(items, index)
             if (validIndex == -1) return@launch

             if (audioJobs.containsKey(validIndex)) return@launch

             val job = async { fetchAudioUrl(items, validIndex, modelId) }
             audioJobs[validIndex] = job
        }
    }

    private suspend fun fetchAudioUrl(items: List<ReaderItem>, index: Int, modelId: String): String? {
        val item = items.getOrNull(index) ?: return null
        val textToSpeak = getTextToSpeak(item)
        
        if (textToSpeak.isNotEmpty()) {
            val cacheKey = "$modelId-$textToSpeak"
            if (urlCache.containsKey(cacheKey)) {
                return urlCache[cacheKey]
            }

            val response = readingVoiceRepository.getVoiceStory(modelId, textToSpeak, "vi")
            if (response is Response.Success) {
                val url = response.data.audio_path
                val finalUrl = url.replace("http://localhost:9000", "https://ctd37qdd-9000.asse.devtunnels.ms").replace("http://127.0.0.1:9000", "https://ctd37qdd-9000.asse.devtunnels.ms")
                
                urlCache[cacheKey] = finalUrl
                return finalUrl
            }
        }
        return null
    }

    private fun findNextValidItemIndex(items: List<ReaderItem>, startIndex: Int): Int {
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
         return if (text.any { it.isLetterOrDigit() }) text else ""
    }

    fun next() {
        appScope.launch {
             val session = readerManager.session ?: return@launch
             val currentItemPos = session.readerTextToSpeech.state.currentActiveItemState.value.itemPos
             val items = session.items
             
             val currentIndex = indexOfReaderItem(
                list = items,
                chapterIndex = currentItemPos.chapterIndex,
                chapterItemPosition = currentItemPos.chapterItemPosition
            )

            if (currentIndex == -1) return@launch
            
            var nextIndex = currentIndex + 1
            while (nextIndex < items.size) {
                 val item = items[nextIndex]
                 if (item is ReaderItem.Position && getTextToSpeak(item).isNotEmpty()) {
                      aiVoicePlayer.stop()
                      playAtIndex(nextIndex, activeVoice.value?.modelId ?: "")
                      return@launch
                 }
                 nextIndex++
            }
            
            // Should handle Next Chapter logic here too if end of chapter?
        }
    }

    fun previous() {
        appScope.launch {
             val session = readerManager.session ?: return@launch
             val currentItemPos = session.readerTextToSpeech.state.currentActiveItemState.value.itemPos
             val items = session.items
             
             val currentIndex = indexOfReaderItem(
                list = items,
                chapterIndex = currentItemPos.chapterIndex,
                chapterItemPosition = currentItemPos.chapterItemPosition
            )
            
            if (currentIndex <= 0) return@launch
            
             var prevIndex = currentIndex - 1
            while (prevIndex >= 0) {
                 val item = items[prevIndex]
                 if (item is ReaderItem.Position && getTextToSpeak(item).isNotEmpty()) {
                      aiVoicePlayer.stop()
                      playAtIndex(prevIndex, activeVoice.value?.modelId ?: "")
                      return@launch
                 }
                 prevIndex--
            }
        }
    }
}
