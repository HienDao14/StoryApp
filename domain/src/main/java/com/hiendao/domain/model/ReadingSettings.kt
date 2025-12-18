package com.hiendao.domain.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

data class ReadingTheme(
    val name: String,
    val textColor: String,
    val backgroundColor: String
)

enum class ReaderTheme(val theme: ReadingTheme) {
    LIGHT(ReadingTheme("Sáng", "#000000", "#FFFFFF")),
    DARK(ReadingTheme("Tối", "#E0E0E0", "#1E1E1E")),
    SEPIA(ReadingTheme("Kem", "#5C4B37", "#F4E4C1")),
    GREEN(ReadingTheme("Xanh", "#2E7D32", "#E8F5E9")),
    GRAY(ReadingTheme("Xám", "#424242", "#F5F5F5")),
    CUSTOM(ReadingTheme("Tùy chỉnh", "#000000", "#FFFFFF"))
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ReadingSettings(
    val isDarkMode: Boolean = false,
    val fontSize: Float = 16f,
    val titleSize: Float = 22f,
    val fontFamily: String = "serif",
    val lineHeight: Float = 1.6f,
    val textColor: String = "#000000",
    val backgroundColor: String = "#FFFFFF",
    val paddingHorizontal: Int = 16,
    val paddingVertical: Int = 8,
    val currentTheme: ReaderTheme = ReaderTheme.LIGHT,
    val allowTextSelection: Boolean = true,
    val keepScreenOn: Boolean = false,
    val fullScreen: Boolean = false
)

data class ReadingProgress(
    val bookId: String,
    val chapterId: String,
    val itemIndex: Int,
    val scrollOffset: Int,
    val totalItems: Int,
    val chapterProgress: Float = 0f, // 0.0 to 1.0
    val bookProgress: Float = 0f, // 0.0 to 1.0
    val lastReadAt: Long = System.currentTimeMillis()
)
