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
    private val libraryBooksRepository: LibraryBooksRepository
) : ViewModel(){
    private var _bookUrl = MutableStateFlow<String>("")
    val bookUrl = _bookUrl.asStateFlow()

    private var _chapterUrl = MutableStateFlow<String>("")
    val chapterUrl = _chapterUrl.asStateFlow()

    private var _bookTitle = MutableStateFlow<String>("")
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
        // giá trị initial nếu cần
        readerManager.initiateOrGetSession("", "")
    )

    val readerSession = _readerSession

    val readingStats = readerSession.map { session ->
        session.readingStats
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    // Speaker stats for tracking audio playback progress
    val speakerStats = readerSession.map { session ->
        session.speakerStats
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    // Current text being played
    val currentTextPlaying = readerSession.map { session ->
        session.readerTextToSpeech.currentTextPlaying
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    // Is currently speaking
    val isSpeaking = readerSession.map { session ->
        session.readerTextToSpeech.isSpeaking
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    private val themeId = appPreferences.THEME_ID.state(viewModelScope)

    val readerState = combine(
        readerSession,
        readingStats
    ) { session, stats ->

        ReaderScreenState(
            showReaderInfo = mutableStateOf(false),
            readerInfo = ReaderScreenState.CurrentInfo(
                chapterTitle = derivedStateOf { stats?.value?.chapterTitle ?: "" },
                chapterCurrentNumber = derivedStateOf {
                    stats?.value?.run { chapterIndex + 1 } ?: 0
                },
                chapterPercentageProgress = session.readingChapterProgressPercentage,
                chaptersCount = derivedStateOf { stats?.value?.chapterCount ?: 0 },
                chapterUrl = derivedStateOf { stats?.value?.chapterUrl ?: "" }
            ),
            settings = ReaderScreenState.Settings(
                selectedSetting = mutableStateOf(ReaderScreenState.Settings.Type.None),
                isTextSelectable = appPreferences.READER_SELECTABLE_TEXT.state(viewModelScope),
                keepScreenOn = appPreferences.READER_KEEP_SCREEN_ON.state(viewModelScope),
                textToSpeech = session.readerTextToSpeech.state,
                liveTranslation = session.readerLiveTranslation.state,
                fullScreen = appPreferences.READER_FULL_SCREEN.state(viewModelScope),
                style = ReaderScreenState.Settings.StyleSettingsData(
                    followSystem = appPreferences.THEME_FOLLOW_SYSTEM.state(viewModelScope),
                    currentTheme = derivedStateOf { themeId.value.toTheme },
                    textFont = appPreferences.READER_FONT_FAMILY.state(viewModelScope),
                    textSize = appPreferences.READER_FONT_SIZE.state(viewModelScope),
                )
            ),
            showVoiceLoadingDialog = mutableStateOf(false),
            showInvalidChapterDialog = mutableStateOf(false)
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
        readerSession.value.readerTextToSpeech.start()
    }
    
    fun pause() {
        readerSession.value.readerTextToSpeech.stop()
    }

    fun playChapterFromStart(chapterUrl: String) {
        viewModelScope.launch {
            // Update the requested chapter URL to trigger session reload
            _chapterUrl.value = chapterUrl
            
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
                            startSpeaker(itemIndex)
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
        }
    }
    
    fun reloadReader() {
        val currentChapter = readingCurrentChapter.copy()
        readerSession.value.reloadReader()
        chaptersLoader.tryLoadRestartedInitial(currentChapter)
    }
    val items = readerSession.value.items
    val chaptersLoader = readerSession.value.readerChaptersLoader
    val readerSpeaker = readerSession.value.readerTextToSpeech
    var readingCurrentChapter by Delegates.observable(readerSession.value.currentChapter) { _, _, new ->
        readerSession.value.currentChapter = new
    }
    val ttsScrolledToTheTop = readerSession.value.readerTextToSpeech.scrolledToTheTop
    val ttsScrolledToTheBottom = readerSession.value.readerTextToSpeech.scrolledToTheBottom
    fun updateInfoViewTo(itemIndex: Int) =
        readerSession.value.updateInfoViewTo(itemIndex = itemIndex)

    fun markChapterStartAsSeen(chapterUrl: String) =
        readerSession.value.markChapterStartAsSeen(chapterUrl = chapterUrl)

    fun markChapterEndAsSeen(chapterUrl: String) =
        readerSession.value.markChapterEndAsSeen(chapterUrl = chapterUrl)

    fun updateState(bookUrl: String, bookTitle: String){
        _bookUrl.value = bookUrl
        _bookTitle.value = bookTitle
        reload()
    }

    init {
        // Essential: Initialize Reader callbacks so that ReaderChaptersLoader executes insert blocks
        readerViewHandlersActions.maintainStartPosition = { it() }
        readerViewHandlersActions.maintainLastVisiblePosition = { it() }
        readerViewHandlersActions.forceUpdateListViewState = { }
        readerViewHandlersActions.setInitialPosition = { }
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
}