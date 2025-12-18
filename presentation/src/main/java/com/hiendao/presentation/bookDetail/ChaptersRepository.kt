package com.hiendao.presentation.bookDetail

import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.coreui.utils.TernaryState
import com.hiendao.domain.repository.AppRepository
import com.hiendao.domain.utils.removeCommonTextFromTitles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChaptersRepository @Inject constructor(
    private val appRepository: AppRepository,
    private val appPreferences: AppPreferences,
) {
    fun getChaptersSortedFlow(bookUrl: String) = appRepository.bookChapters
        .getChaptersWithContextFlow(bookUrl = bookUrl)
        .map(::removeCommonTextFromTitles)
        // Sort the chapters given the order preference
        .combine(appPreferences.CHAPTERS_SORT_ASCENDING.flow()) { chapters, sorted ->
            when (sorted) {
                TernaryState.Active -> chapters.sortedBy { it.chapter.position }
                TernaryState.Inverse -> chapters.sortedByDescending { it.chapter.position }
                TernaryState.Inactive -> chapters
            }
        }
        .flowOn(Dispatchers.Default)

    suspend fun getLastReadChapter(bookUrl: String): String? =
        appRepository.libraryBooks.get(bookUrl)?.lastReadChapter
            ?: appRepository.bookChapters.getFirstChapter(bookUrl)?.id

}