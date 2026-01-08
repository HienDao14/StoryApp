package com.hiendao.presentation.voice.viewModel

import android.net.Uri
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiendao.coreui.R
import com.hiendao.coreui.appPreferences.AppPreferences
import my.noveldokusha.features.reader.ui.ReaderViewHandlersActions
import com.hiendao.coreui.theme.toTheme
import com.hiendao.coreui.utils.Toasty
import com.hiendao.coreui.utils.toState
import com.hiendao.data.local.entity.ChapterWithContext
import com.hiendao.data.utils.AppCoroutineScope
import com.hiendao.domain.map.toDomain
import com.hiendao.domain.repository.AppRepository
import com.hiendao.domain.repository.LibraryBooksRepository
import com.hiendao.domain.utils.AppFileResolver
import com.hiendao.presentation.bookDetail.ChaptersRepository
import com.hiendao.presentation.bookDetail.state.ChaptersScreenState
import com.hiendao.presentation.reader.manager.ReaderManager
import com.hiendao.presentation.reader.ui.ReaderScreenState
import com.hiendao.presentation.voice.ReadingVoiceRepository
import com.hiendao.presentation.voice.state.VoiceReaderScreenState
import com.hiendao.presentation.voice.state.VoiceReaderScreenState.BookState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates
import com.hiendao.presentation.reader.features.AiVoicePlayer
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import com.hiendao.coreui.appPreferences.VoicePredefineState
import com.hiendao.domain.utils.Response
import com.hiendao.presentation.reader.domain.ReaderItem
import com.hiendao.presentation.reader.domain.indexOfReaderItem
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import com.hiendao.coreui.utils.StateExtra_Boolean
import com.hiendao.coreui.utils.StateExtra_String
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context

import kotlinx.coroutines.flow.flatMapLatest
import androidx.compose.runtime.snapshotFlow
import com.hiendao.presentation.reader.domain.ChapterState


@HiltViewModel
internal class VoiceViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val appScope: AppCoroutineScope,
    private val toasty: Toasty,
    appPreferences: AppPreferences,
    appFileResolver: AppFileResolver,
    stateHandle: SavedStateHandle,
    private val chaptersRepository: ChaptersRepository,
    private val readerManager: ReaderManager,
    private val readerViewHandlersActions: ReaderViewHandlersActions,
    private val libraryBooksRepository: LibraryBooksRepository,
    private val readingVoiceRepository: ReadingVoiceRepository,
    private val aiNarratorManager: com.hiendao.presentation.voice.manager.AiNarratorManager,
    @ApplicationContext private val context: Context,
) : ViewModel(){

    private val initialBookUrl = stateHandle.get<String>("bookId") ?: ""
    private val initialBookTitle = stateHandle.get<String>("bookTitle") ?: ""
    
    private val initialChapterUrl : String = readerManager.session?.let { session ->
        if (session.bookUrl == initialBookUrl) session.currentChapter.chapterUrl else ""
    } ?: ""

    private var _bookUrl = MutableStateFlow<String>(initialBookUrl)
    val bookUrl = _bookUrl.asStateFlow()

    private var _chapterUrl = MutableStateFlow<String>(initialChapterUrl)
    val chapterUrl = _chapterUrl.asStateFlow()

    private var _bookTitle = MutableStateFlow<String>(initialBookTitle)
    val bookTitle = _bookTitle.asStateFlow()


    @Volatile
    private var lastSelectedChapterUrl: String? = null
    private val book = appRepository.libraryBooks.getFlow(bookUrl.value)
        .filterNotNull()
        .map { BookState(it.toDomain()) }
        .toState(
            viewModelScope,
            BookState(title = bookTitle.value, url = bookUrl.value, coverImageUrl = null)
        )

    private val _bookState = MutableStateFlow<BookState>(BookState(title = bookTitle.value, url = bookUrl.value, coverImageUrl = null))
    val bookState = _bookState.asStateFlow()

    private val _readerSession = combine(bookUrl, chapterUrl) { book, chapter ->
        readerManager.initiateOrGetSession(
            bookUrl = book,
            chapterUrl = chapter
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        readerManager.initiateOrGetSession(initialBookUrl, initialChapterUrl)
    )

    val readerSession = _readerSession

    val readingStats = readerSession.flatMapLatest { session ->
        snapshotFlow { session.readingStats.value }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    // Speaker stats for tracking audio playback progress
    val speakerStats = readerSession.flatMapLatest { session ->
        snapshotFlow { session.speakerStats.value }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    // Current text being played
    val currentTextPlaying = readerSession.flatMapLatest { session ->
        snapshotFlow { session.readerTextToSpeech.currentTextPlaying.value }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    // Is currently speaking
    val isSpeaking = readerSession.flatMapLatest { session ->
        snapshotFlow { session.readerTextToSpeech.isSpeaking.value }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    private val themeId = appPreferences.THEME_ID.state(viewModelScope)

    // Wrap TTS state to intercept controls
    private val wrappedTtsStateFlow = readerSession.map { session ->
        val originalTtsState = session.readerTextToSpeech.state
        originalTtsState.copy(
            setPlaying = { isPlaying ->
                if (aiNarratorManager.activeVoice.value != null) {
                    if (isPlaying) aiNarratorManager.resume() else aiNarratorManager.pause()
                } else {
                    originalTtsState.setPlaying(isPlaying)
                }
            },
            playNextItem = {
                 if (aiNarratorManager.activeVoice.value != null) aiNarratorManager.next() else originalTtsState.playNextItem()
            },
            playPreviousItem = {
                 if (aiNarratorManager.activeVoice.value != null) aiNarratorManager.previous() else originalTtsState.playPreviousItem()
            },
            playNextChapter = {
                 // TODO: Move Chapter logic to Manager if needed, for now keep basic nav
                 if (aiNarratorManager.activeVoice.value != null) {
                     // Basic next chapter implementation if Manager doesn't support it yet
                     originalTtsState.playNextChapter()
                     // But wait, original moves the cursor. Then we need to tell Manager to play new pos.
                     // The Manager observes session events via UI? No. 
                     // For now fallback to simple item nav or let user select chapter manually.
                     // Ideally implement nextChapter in Manager.
                 } else originalTtsState.playNextChapter()
            },
            playPreviousChapter = {
                 if (aiNarratorManager.activeVoice.value != null) {
                    originalTtsState.playPreviousChapter()
                 } else originalTtsState.playPreviousChapter()
            },
            setVoiceId = { voiceId ->
                aiNarratorManager.stop()
                aiNarratorManager.setActiveVoice(null)
                originalTtsState.setVoiceId(voiceId)
            }
        )
    }

    private val _showVoiceLoadingDialog = mutableStateOf(false)
    private val _showInvalidChapterDialog = mutableStateOf(false)

    val readerState = combine(
        readerSession,
        readingStats,
        wrappedTtsStateFlow
    ) { session, stats, wrappedTtsState ->
 
         // Sync player state
         // Note: calling collect inside combine is bad practice as it suspends. 
         // We should set up a separate collector in init.
         
        ReaderScreenState(
            showReaderInfo = mutableStateOf(false),
            readerInfo = ReaderScreenState.CurrentInfo(
                chapterTitle = derivedStateOf { stats?.chapterTitle ?: "" },
                chapterCurrentNumber = derivedStateOf {
                    stats?.run { chapterIndex + 1 } ?: 0
                },
                chapterPercentageProgress = session.readingChapterProgressPercentage,
                chaptersCount = derivedStateOf { stats?.chapterCount ?: 0 },
                chapterUrl = mutableStateOf(stats?.chapterUrl ?: "")
            ),
            settings = ReaderScreenState.Settings(
                selectedSetting = mutableStateOf(ReaderScreenState.Settings.Type.None),
                isTextSelectable = appPreferences.READER_SELECTABLE_TEXT.state(viewModelScope),
                keepScreenOn = appPreferences.READER_KEEP_SCREEN_ON.state(viewModelScope),
                textToSpeech = wrappedTtsState,
                liveTranslation = session.readerLiveTranslation.state,
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
                ),
                
            ),
            showVoiceLoadingDialog = _showVoiceLoadingDialog,
            showInvalidChapterDialog = _showInvalidChapterDialog
        )

    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        // initial (tuỳ bạn)
        null
    )

    private val chapters: SnapshotStateList<ChapterWithContext> = mutableStateListOf()
    private val _chapters: MutableStateFlow<SnapshotStateList<ChapterWithContext>> = MutableStateFlow(mutableStateListOf())
    val chaptersFlow = _chapters.asStateFlow()
    val state = combine(
        readerState,
        speakerStats,
        currentTextPlaying,
        isSpeaking,
        chaptersFlow
    ) { reader, stats, currentText, speaking, chapters ->
        VoiceReaderScreenState(
            book = bookState.toState(viewModelScope, BookState(title = bookTitle.value, url = bookUrl.value, coverImageUrl = null)),
            error = mutableStateOf(""),
            chapters = chapters,
            selectedChaptersUrl = mutableStateMapOf(),
            settingChapterSort = appPreferences.CHAPTERS_SORT_ASCENDING.state(viewModelScope),
            readerState = reader,
            speakerStats = stats,
            currentTextPlaying = currentText,
            isSpeaking = speaking
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        VoiceReaderScreenState(
            book = bookState.toState(viewModelScope, BookState(title = bookTitle.value, url = bookUrl.value, coverImageUrl = null)),
            error = mutableStateOf(""),
            chapters = chapters,
            selectedChaptersUrl = mutableStateMapOf(),
            settingChapterSort = appPreferences.CHAPTERS_SORT_ASCENDING.state(viewModelScope),
            readerState = null,
            speakerStats = null,
            currentTextPlaying = null,
            isSpeaking = null
        )
    )

    fun startSpeaker(itemIndex: Int) =
        readerSession.value.startSpeaker(itemIndex = itemIndex)
    
    fun play() {
        if (aiNarratorManager.activeVoice.value != null) {
            aiNarratorManager.resume()
             // Ensure service is started
             com.hiendao.presentation.reader.services.NarratorMediaControlsService.start(context)
        } else {
            readerSession.value.readerTextToSpeech.state.setPlaying(true)
        }
    }
    
    fun pause() {
        if (aiNarratorManager.activeVoice.value != null) {
            aiNarratorManager.pause()
        } else {
            readerSession.value.readerTextToSpeech.state.setPlaying(false)
        }
    }

    fun playChapterFromStart(chapterUrl: String) {
        viewModelScope.launch {
            // Update the requested chapter URL to trigger session reload
            _chapterUrl.value = chapterUrl
            readerState?.value?.readerInfo?.chapterUrl?.value = chapterUrl
            
            // Wait for the session to update and items to be loaded for the new chapter
            var retry = 0
            while (retry < 20) {
                val session = readerSession.value
                val sessionChapterUrl = session.currentChapter.chapterUrl
                val items = session.items
                
                if (sessionChapterUrl == chapterUrl && items.isNotEmpty()) {
                    // Session is ready. Find the start item of the chapter.
                    val chapterIndex = state.value.chapters.indexOfFirst { it.chapter.id == chapterUrl }
                     if (chapterIndex != -1) {
                        val itemIndex = com.hiendao.presentation.reader.domain.indexOfReaderItem(
                            list = items,
                            chapterIndex = chapterIndex,
                            chapterItemPosition = 0
                        )
                        if (itemIndex != -1) {
                            if (aiNarratorManager.activeVoice.value != null) {
                                // If AI voice is active, tell manager to play from this index
                                // But manager needs strict item index.
                                // We can just tell Manager to playForCurrent() after setting position?
                                // startSpeaker(itemIndex) sets the position in session manager?
                                // Yes: readerSession.value.startSpeaker calls manager.setCurrentSpeakState
                                startSpeaker(itemIndex)
                                aiNarratorManager.playForCurrent()
                            } else {
                                startSpeaker(itemIndex)
                            }
                            return@launch
                        }
                    }
                }
                kotlinx.coroutines.delay(200)
                retry++
            }
        }
    }

    fun autoPlay() {
        viewModelScope.launch {
            // Wait for items to load
            var retry = 0
            while ((readerSession.value.items.isEmpty() || chapters.isEmpty()) && retry < 20) {
                kotlinx.coroutines.delay(200)
                retry++
            }
            if (readerSession.value.items.isEmpty()) return@launch

            val currentChapter = readerSession.value.currentChapter
            val chapterUrl = currentChapter.chapterUrl
            val chapterIndex = chapters.indexOfFirst { it.chapter.id == chapterUrl } // Find index in loaded chapters
            
            if (chapterIndex != -1) {
                 val itemIndex = com.hiendao.presentation.reader.domain.indexOfReaderItem(
                    list = readerSession.value.items,
                    chapterIndex = chapterIndex,
                    chapterItemPosition = currentChapter.chapterItemPosition
                )
                if (itemIndex != -1) {
                    startSpeaker(itemIndex)
                } else {
                     // Fallback to start of the chapter
                     val startIdx = com.hiendao.presentation.reader.domain.indexOfReaderItem(
                        list = readerSession.value.items,
                        chapterIndex = chapterIndex,
                        chapterItemPosition = 0
                     )
                     if (startIdx != -1) startSpeaker(startIdx)
                }
            } else {
                // If chapter not found, maybe just play first available
                startSpeaker(0)
            }
            // Auto play trigger
            if (readerState.value?.settings?.textToSpeech?.isPlaying?.value == true) {
                 play()
            }
        }
    }
    
    fun reloadReader() {
        val currentChapter = readingCurrentChapter.copy()
        readerSession.value.reloadReader()
        chaptersLoader.tryLoadRestartedInitial(currentChapter)
    }
    val items: List<ReaderItem>
        get() = readerSession.value.items

    val chaptersLoader
        get() = readerSession.value.readerChaptersLoader

    val readerSpeaker
        get() = readerSession.value.readerTextToSpeech

    var readingCurrentChapter: ChapterState
        get() = readerSession.value.currentChapter
        set(value) {
            readerSession.value.currentChapter = value
        }
    val ttsScrolledToTheTop
        get() = readerSession.value.readerTextToSpeech.scrolledToTheTop
    val ttsScrolledToTheBottom
        get() = readerSession.value.readerTextToSpeech.scrolledToTheBottom
    fun updateInfoViewTo(itemIndex: Int) =
        readerSession.value.updateInfoViewTo(itemIndex = itemIndex)

    fun markChapterStartAsSeen(chapterUrl: String) =
        readerSession.value.markChapterStartAsSeen(chapterUrl = chapterUrl)

    fun markChapterEndAsSeen(chapterUrl: String) =
        readerSession.value.markChapterEndAsSeen(chapterUrl = chapterUrl)

    fun updateState(bookUrl: String, bookTitle: String){
        state.value.isInitializing.value = true
        _bookUrl.value = bookUrl
        _bookTitle.value = bookTitle
        
        // If there is an active session for this book, use its chapter to init properly
        val activeSession = readerManager.session
        if (activeSession != null && activeSession.bookUrl == bookUrl) {
            _chapterUrl.value = activeSession.currentChapter.chapterUrl
        }
        
        reload()
        
        viewModelScope.launch {
            // Wait for items to be loaded
            var retry = 0
            while (readerSession.value.items.isEmpty() && retry < 20) {
                kotlinx.coroutines.delay(200)
                retry++
            }
            if (readerSession.value.items.isNotEmpty()) {
                restoreLastReadPosition()
            }
            state.value.isInitializing.value = false
        }
    }
    
    private fun restoreLastReadPosition() {
        val session = readerSession.value
        val items = session.items
        if (items.isEmpty()) return

        val currentChapterUrl = session.currentChapter.chapterUrl
        readerState?.value?.readerInfo?.chapterUrl?.value = currentChapterUrl

        // Find the chapter object to get lastReadPosition
        // We use the repository/state chapters to get the latest DB value
        val chapter = state.value.chapters.find { it.chapter.id == currentChapterUrl }?.chapter ?: return
        val lastReadPos = chapter.lastReadPosition
        
        // Find internal chapter index using Title item
        val titleItem = items.find { it is ReaderItem.Title && it.chapterUrl == currentChapterUrl }
        val chapterIndex = (titleItem as? ReaderItem.Title)?.chapterIndex ?: return
        
        val itemIndex = com.hiendao.presentation.reader.domain.indexOfReaderItem(
            list = items,
            chapterIndex = chapterIndex,
            chapterItemPosition = lastReadPos
        )
        
        if (itemIndex != -1) {
            session.prepareSpeaker(itemIndex)
            updateInfoViewTo(itemIndex)
        }
    }

    init {
        // Essential: Initialize Reader callbacks so that ReaderChaptersLoader executes insert blocks
        readerViewHandlersActions.maintainStartPosition = { it() }
        readerViewHandlersActions.maintainLastVisiblePosition = { it() }
        readerViewHandlersActions.forceUpdateListViewState = { }
        readerViewHandlersActions.setInitialPosition = { }
        
        viewModelScope.launch {
            aiNarratorManager.isLoading.collect { loading ->
                _showVoiceLoadingDialog.value = loading
            }
        }
    }

    fun reload(){
        viewModelScope.launch {
            if (_chapterUrl.value.isEmpty()) {
                val last = chaptersRepository.getLastReadChapter(bookUrl.value)
                if (last != null) _chapterUrl.value = last
            }

            launch {
                chaptersRepository.getChaptersSortedFlow(bookUrl = bookUrl.value).collect {
                    state.value.chapters.clear()
                    state.value.chapters.addAll(it)
                }
            }
            launch {
                appRepository.libraryBooks.getFlow(bookUrl.value)
                    .filterNotNull()
                    .map { BookState(it.toDomain()) }.collect {
                        _bookState.value = it
                    }
            }
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            launch {
                libraryBooksRepository.toggleFavourite(bookUrl.value)
            }
            val isBookmarked =
                appRepository.toggleBookmark(bookTitle = bookTitle.value, bookUrl = bookUrl.value)
            val msg = if (isBookmarked) R.string.added_to_library else R.string.removed_from_library
            reload()
            toasty.show(msg)
        }
    }

    fun saveImageAsCover(uri: Uri) {
        appRepository.libraryBooks.saveImageAsCover(imageUri = uri, bookUrl = bookUrl.value)
    }

    // AI Voice Logic

    fun selectModelVoice(voice: VoicePredefineState) {
        val ttsState = readerState.value?.settings?.textToSpeech ?: return
        
        // Check if currently playing
        val wasPlaying = ttsState.isPlaying.value == true

        // Stop current
        aiNarratorManager.stop()
        readerSession.value.readerTextToSpeech.stop()
        
        aiNarratorManager.setActiveVoice(voice)
        
        if (wasPlaying) {
            viewModelScope.launch {
                 // Ensure UI and Manager are synced
                 aiNarratorManager.playForCurrent()
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Do NOT stop aiNarratorManager here if we want persistence!
        // But if we want to release resources when NOT playing?
        // Let Manager handle its own lifecycle or explicit stop. 
        // For now, removing onCleared cleanup ensures persistence.
        // aiVoicePlayer.release() // Removed
    }
}
