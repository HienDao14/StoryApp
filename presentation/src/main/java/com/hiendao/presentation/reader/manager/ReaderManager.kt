package com.hiendao.presentation.reader.manager

import kotlinx.coroutines.flow.asStateFlow
import my.noveldokusha.features.reader.ui.ReaderViewHandlersActions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ReaderManager @Inject constructor(
    private val readerSessionProvider: ReaderSessionProvider,
    private val readerViewHandlersActions: ReaderViewHandlersActions
) {
    var session: ReaderSession? = null
        private set


    private val _sessionFlow = kotlinx.coroutines.flow.MutableStateFlow<ReaderSession?>(null)
    val sessionFlow = _sessionFlow.asStateFlow()

    fun initiateOrGetSession(
        bookUrl: String,
        chapterUrl: String,
    ): ReaderSession {
        val currentSession = session
        if (currentSession != null && bookUrl == currentSession.bookUrl && chapterUrl == currentSession.currentChapter.chapterUrl) {
            readerViewHandlersActions.introScrollToCurrentChapter = true
            return currentSession
        }

        currentSession?.close()
        readerViewHandlersActions.introScrollToCurrentChapter = false

        val newSession = readerSessionProvider.create(
            bookUrl = bookUrl,
            initialChapterUrl = chapterUrl,
        )
        session = newSession
        _sessionFlow.value = newSession
        newSession.init()

        return newSession
    }

    fun close() {
        session?.close()
        session = null
        _sessionFlow.value = null
    }
}
