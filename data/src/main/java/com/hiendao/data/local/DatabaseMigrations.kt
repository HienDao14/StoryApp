package com.hiendao.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Centralized database migrations management
 * 
 * IMPORTANT RULES:
 * 1. Always increment version number when changing schema
 * 2. Always add migration for version changes
 * 3. Test migrations before release
 * 4. Document all schema changes
 * 
 * Version History:
 * - Version 1: Initial database
 * - Version 2: Changed isSelected to isSelectedLock and isSelectedHome in WallpaperEntity
 * - Version 3: Added lastSetLockTime and lastSetHomeTime columns to track wallpaper history timestamps
 * - Version 4: Added is_test_data column to identify test data
 * - Version 5: Ensure all columns exist (migration fix)
 * - Version 6: No schema changes
 * - Version 7: Added isSelectedHome and isSelectedLock to ZipperImageEntity (recreated table with indices)
 */
object DatabaseMigrations {
    
    /**
     * Get all migrations for Room database builder
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            // Add future migrations here
        )
    }
}