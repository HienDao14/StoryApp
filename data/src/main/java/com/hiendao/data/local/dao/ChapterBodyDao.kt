package com.hiendao.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hiendao.data.local.entity.ChapterBodyEntity

@Dao
abstract class ChapterBodyDao {
    @Query("SELECT * FROM ChapterBodyEntity")
    abstract suspend fun getAll(): List<ChapterBodyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertReplace(chapterBodyEntity: ChapterBodyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertReplace(chapterBodyEntity: List<ChapterBodyEntity>)

    @Query("SELECT * FROM ChapterBodyEntity WHERE chapterId = :url")
    abstract suspend fun get(url: String): ChapterBodyEntity?

    @Query("DELETE FROM ChapterBodyEntity WHERE ChapterBodyEntity.chapterId NOT IN (SELECT chapterId FROM ChapterEntity)")
    abstract suspend fun removeAllNonChapterRows()

    @Query("DELETE FROM ChapterBodyEntity WHERE ChapterBodyEntity.chapterId IN (:chaptersUrl)")
    abstract suspend fun removeChapterRows(chaptersUrl: List<String>)
}