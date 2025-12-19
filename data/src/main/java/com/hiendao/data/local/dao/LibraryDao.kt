package com.hiendao.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hiendao.data.local.entity.BookEntity
import com.hiendao.data.local.entity.BookWithContext
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LibraryDao {
    @Query("SELECT * FROM BookEntity WHERE inLibrary == 1")
    abstract suspend fun getAll(): List<BookEntity>

    @Query("SELECT * FROM BookEntity WHERE inLibrary == 1")
    abstract suspend fun getAllInLibrary(): List<BookEntity>

    @Query("SELECT * FROM BookEntity WHERE inLibrary == 1")
    abstract fun booksInLibraryFlow(): Flow<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(book: List<BookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertReplace(book: List<BookEntity>)

    @Delete
    abstract suspend fun remove(book: BookEntity)

    @Query("DELETE FROM BookEntity WHERE BookEntity.id = :bookUrl")
    abstract suspend fun remove(bookUrl: String)

    @Update
    abstract suspend fun update(book: BookEntity)

    @Query("UPDATE BookEntity SET coverImageUrl = :coverUrl WHERE id == :bookUrl")
    abstract suspend fun updateCover(bookUrl: String, coverUrl: String)

    @Query("UPDATE BookEntity SET lastReadEpochTimeMilli = :lastReadEpochTimeMilli WHERE id == :bookUrl")
    abstract suspend fun updateLastReadEpochTimeMilli(bookUrl: String, lastReadEpochTimeMilli: Long)

    @Query("UPDATE BookEntity SET description = :description WHERE id == :bookUrl")
    abstract suspend fun updateDescription(bookUrl: String, description: String)

    @Query("UPDATE BookEntity SET lastReadChapter = :chapterUrl WHERE id == :bookUrl")
    abstract suspend fun updateLastReadChapter(bookUrl: String, chapterUrl: String)

    @Query("SELECT * FROM BookEntity WHERE id = :url")
    abstract suspend fun get(url: String): BookEntity?

    @Query("SELECT * FROM BookEntity WHERE id = :url")
    abstract fun getFlow(url: String): Flow<BookEntity?>

    @Query("SELECT EXISTS(SELECT * FROM BookEntity WHERE id == :url AND inLibrary == 1)")
    abstract suspend fun existInLibrary(url: String): Boolean

    @Query(
        """
        SELECT BookEntity.*, COUNT(ChapterEntity.read) AS chaptersCount, SUM(ChapterEntity.read) AS chaptersReadCount
        FROM BookEntity
        LEFT JOIN ChapterEntity ON ChapterEntity.bookId = BookEntity.id
        WHERE BookEntity.inLibrary == 1
        GROUP BY BookEntity.id
    """
    )
    abstract fun getBooksInLibraryWithContextFlow(): Flow<List<BookWithContext>>

    @Query("DELETE FROM BookEntity WHERE inLibrary == 0")
    abstract suspend fun removeAllNonLibraryRows()

    @Query("SELECT * FROM bookentity WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    abstract suspend fun searchBooks(query: String): List<BookEntity>

}