package com.hiendao.domain.repository

import android.content.Context
import android.content.SharedPreferences
import com.hiendao.domain.mockData.MockDataProvider
import com.hiendao.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BooksRepository @Inject constructor(
    private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("novel_reader", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    // In-memory demo stores
    private val _books = MutableStateFlow(MockDataProvider.getMockBooks())
    private val _chapters = MutableStateFlow<Map<String, List<Chapter>>>(emptyMap())
    private val _readerItems = MutableStateFlow<Map<String, List<ReaderItem>>>(emptyMap())
    private val _readingSettings = MutableStateFlow(loadReadingSettings())
    private val _readingProgress = MutableStateFlow<Map<String, ReadingProgress>>(emptyMap())
    // ^ key = bookId, value = latest progress for that book

    val books: Flow<List<Book>> = _books.asStateFlow()
    val readingSettings: Flow<ReadingSettings> = _readingSettings.asStateFlow()

    init {
        initializeMockData()
    }

    private fun initializeMockData() {
        val mockBooks = MockDataProvider.getMockBooks()
        val chaptersMap = mutableMapOf<String, List<Chapter>>()
        val readerItemsMap = mutableMapOf<String, List<ReaderItem>>()

        mockBooks.forEach { book ->
            val chapters = MockDataProvider.getMockChapters(book.id)
            chaptersMap[book.id] = chapters

            chapters.forEach { chapter ->
                val items = MockDataProvider.getMockReaderItems(chapter.id)
                readerItemsMap[chapter.id] = items
            }
        }

        _chapters.value = chaptersMap
        _readerItems.value = readerItemsMap
    }

    /* -------------------------------- Books -------------------------------- */

    suspend fun getBooks(): List<Book> = _books.value

    suspend fun getBook(bookId: String): Book? =
        _books.value.find { it.id == bookId }

    suspend fun updateBook(book: Book) {
        val current = _books.value.toMutableList()
        val idx = current.indexOfFirst { it.id == book.id }
        if (idx >= 0) {
            current[idx] = book
            _books.value = current
        }
    }

    /* ------------------------------- Chapters ------------------------------- */

    suspend fun getChapters(bookId: String): List<Chapter> =
        _chapters.value[bookId] ?: emptyList()

    suspend fun getChapter(chapterId: String): Chapter? =
        _chapters.value.values.flatten().find { it.id == chapterId }

    suspend fun updateChapter(chapter: Chapter) {
        val map = _chapters.value.toMutableMap()
        val list = map[chapter.bookId]?.toMutableList() ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == chapter.id }
        if (idx >= 0) {
            list[idx] = chapter
            map[chapter.bookId] = list
            _chapters.value = map
        }
    }

    // NEW: lấy chapter theo order trong 1 book
    suspend fun getChapterByOrder(bookId: String, order: Int): Chapter? =
        getChapters(bookId).firstOrNull { it.order == order }

    /* ------------------------------ ReaderItems ----------------------------- */

    suspend fun getReaderItems(chapterId: String): List<ReaderItem> =
        _readerItems.value[chapterId] ?: emptyList()

    /* --------------------------- Reading Settings --------------------------- */

    suspend fun getReadingSettings(): ReadingSettings = _readingSettings.value

    suspend fun updateReadingSettings(settings: ReadingSettings) {
        _readingSettings.value = settings
        saveReadingSettings(settings)
    }

    private fun loadReadingSettings(): ReadingSettings {
        val s = prefs.getString("reading_settings", null)
        return if (s != null) {
            try { json.decodeFromString<ReadingSettings>(s) }
            catch (_: Exception) { MockDataProvider.getDefaultReadingSettings() }
        } else {
            MockDataProvider.getDefaultReadingSettings()
        }
    }

    private fun saveReadingSettings(settings: ReadingSettings) {
        prefs.edit().putString("reading_settings", json.encodeToString(settings)).apply()
    }

    /* --------------------------- Reading Progress --------------------------- */

    suspend fun getReadingProgress(bookId: String): ReadingProgress? =
        _readingProgress.value[bookId]

    suspend fun updateReadingProgress(progress: ReadingProgress) {
        // 1) cập nhật cache progress theo book
        val map = _readingProgress.value.toMutableMap()
        map[progress.bookId] = progress
        _readingProgress.value = map

        // 2) update Book.lastRead*
        getBook(progress.bookId)?.let { b ->
            updateBook(
                b.copy(
                    lastReadChapter = progress.chapterId,
                    lastReadPosition = progress.itemIndex,
                    lastReadOffset = progress.scrollOffset,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        // 3) update trạng thái từng Chapter (isRead/readProgress/lastRead*)
        getChapter(progress.chapterId)?.let { ch ->
            val isRead = progress.chapterProgress >= 0.9f
            updateChapter(
                ch.copy(
                    isRead = isRead,
                    readProgress = progress.chapterProgress,
                    lastReadPosition = progress.itemIndex,
                    lastReadOffset = progress.scrollOffset,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // NEW: trả về % đã đọc của TẤT CẢ chapter trong 1 book (để hiển thị trong ChapterPicker)
    suspend fun getAllChapterProgress(bookId: String): Map<String, Float> {
        // Ưu tiên từ dữ liệu Chapter.readProgress (đã được update trong updateReadingProgress)
        val chapters = getChapters(bookId)
        if (chapters.isEmpty()) return emptyMap()
        return chapters.associate { it.id to it.readProgress.coerceIn(0f, 1f) }
    }

    // (giữ lại) % chương hiện tại dựa theo index items – nếu bạn đã tính theo pixel, có thể truyền số đó vào updateReadingProgress
    suspend fun calculateChapterProgress(chapterId: String, currentItemIndex: Int): Float {
        val items = getReaderItems(chapterId)
        return if (items.isEmpty()) 0f
        else (currentItemIndex.toFloat() / items.size.toFloat()).coerceIn(0f, 1f)
    }

    suspend fun calculateBookProgress(bookId: String): Float {
        val chapters = getChapters(bookId)
        if (chapters.isEmpty()) return 0f
        val readChapters = chapters.count { it.isRead }
        return readChapters.toFloat() / chapters.size.toFloat()
    }

    suspend fun updateChapterReadProgress(chapterId: String, progress: Float) {
        val p = progress.coerceIn(0f, 1f)
        val ch = getChapter(chapterId) ?: return
        updateChapter(
            ch.copy(
                isRead = p >= 0.9f,
                readProgress = p,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
