package com.hiendao.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hiendao.data.local.dao.ChapterBodyDao
import com.hiendao.data.local.dao.ChapterDao
import com.hiendao.data.local.dao.LibraryDao
import com.hiendao.data.local.entity.BookEntity
import com.hiendao.data.local.entity.ChapterBodyEntity
import com.hiendao.data.local.entity.ChapterEntity

@Database(entities = [BookEntity::class, ChapterEntity::class, ChapterBodyEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract val libraryDao: LibraryDao
    abstract val chapterDao: ChapterDao
    abstract val chapterBodyDao: ChapterBodyDao
}
