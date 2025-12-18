package com.hiendao.presentation.reader.manager

import android.content.Context
import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.domain.repository.AppRepository
import com.hiendao.domain.translator.TranslationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import com.hiendao.presentation.reader.ReaderRepository
import my.noveldokusha.features.reader.ui.ReaderViewHandlersActions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ReaderSessionProvider @Inject constructor(
    private val appRepository: AppRepository,
    private val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context,
    private val translationManager: TranslationManager,
    private val readerRepository: ReaderRepository,
    private val readerViewHandlersActions: ReaderViewHandlersActions,
) {
    fun create(
        bookUrl: String,
        initialChapterUrl: String,
    ): ReaderSession = ReaderSession(
        bookUrl = bookUrl,
        initialChapterUrl = initialChapterUrl,
        appRepository = appRepository,
        translationManager = translationManager,
        appPreferences = appPreferences,
        context = context,
        readerRepository = readerRepository,
        readerViewHandlersActions = readerViewHandlersActions,
    )
}
