package com.hiendao.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hiendao.data.local.entity.ChapterEntity
import com.hiendao.data.local.entity.ChapterWithContext
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ChapterDao {
    @Query("SELECT * FROM ChapterEntity")
    abstract suspend fun getAll(): List<ChapterEntity>

    @Query(
        """
        SELECT * FROM ChapterEntity
        WHERE ChapterEntity.bookId == :bookUrl
        ORDER BY ChapterEntity.position ASC
    """
    )
    abstract suspend fun chapters(bookUrl: String): List<ChapterEntity>

    @Update
    abstract suspend fun update(chapter: ChapterEntity)

    @Query("SELECT EXISTS(SELECT * FROM ChapterEntity WHERE ChapterEntity.bookId = :bookUrl LIMIT 1)")
    abstract suspend fun hasChapters(bookUrl: String): Boolean

    @Query(
        """
        SELECT * FROM ChapterEntity
        WHERE ChapterEntity.bookId = :bookUrl
        ORDER BY ChapterEntity.position ASC
        LIMIT 1
    """
    )
    abstract suspend fun getFirstChapter(bookUrl: String): ChapterEntity?

    @Query("UPDATE ChapterEntity SET read = 1 WHERE id in (:chaptersUrl)")
    abstract suspend fun setAsRead(chaptersUrl: List<String>)

    @Query("UPDATE ChapterEntity SET read = :read WHERE id = :chapterUrl")
    abstract suspend fun setAsRead(chapterUrl: String, read: Boolean)

    @Query(
        """
        UPDATE ChapterEntity 
        SET lastReadPosition = :lastReadPosition, lastReadOffset = :lastReadOffset
        WHERE id = :chapterUrl
    """
    )
    abstract suspend fun updatePosition(chapterUrl: String, lastReadPosition: Int, lastReadOffset: Int)

    @Query("UPDATE ChapterEntity SET title = :title WHERE id == :url")
    abstract suspend fun updateTitle(url: String, title: String)

    @Query("UPDATE ChapterEntity SET read = 0 WHERE id in (:chaptersUrl)")
    abstract suspend fun setAsUnread(chaptersUrl: List<String>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(chapters: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertReplace(chapters: List<ChapterEntity>)

    @Query("SELECT * FROM ChapterEntity WHERE id = :url")
    abstract suspend fun get(url: String): ChapterEntity?

    @Query("DELETE FROM ChapterEntity WHERE ChapterEntity.bookId = :bookUrl")
    abstract suspend fun removeAllFromBook(bookUrl: String)

    @Query("DELETE FROM ChapterEntity WHERE ChapterEntity.bookId NOT IN (SELECT BookEntity.id FROM BookEntity)")
    abstract suspend fun removeAllNonLibraryRows()

    @Query(
        """
        SELECT ChapterEntity.*, ChapterBodyEntity.chapterId IS NOT NULL AS downloaded , BookEntity.lastReadChapter IS NOT NULL AS lastReadChapter
        FROM ChapterEntity
        LEFT JOIN ChapterBodyEntity ON ChapterBodyEntity.chapterId = ChapterEntity.id
        LEFT JOIN BookEntity ON BookEntity.id = :bookUrl AND BookEntity.lastReadChapter == ChapterEntity.id
        WHERE ChapterEntity.bookId == :bookUrl
        ORDER BY position ASC
    """
    )
    abstract fun getChaptersWithContextFlow(bookUrl: String): Flow<List<ChapterWithContext>>
}