package com.hiendao.presentation.reader.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import com.hiendao.coreui.theme.Themes
import com.hiendao.presentation.reader.features.LiveTranslationSettingData
import com.hiendao.presentation.reader.features.TextToSpeechSettingData

internal data class ReaderScreenState(
    val showReaderInfo: MutableState<Boolean>,
    val readerInfo: CurrentInfo,
    val settings: Settings,
    val showInvalidChapterDialog: MutableState<Boolean>
) {
    data class CurrentInfo(
        val chapterTitle: State<String>,
        val chapterCurrentNumber: State<Int>,
        val chapterPercentageProgress: State<Float>,
        val chaptersCount: State<Int>,
        val chapterUrl: State<String>
    )

    data class Settings(
        val isTextSelectable: State<Boolean>,
        val keepScreenOn: State<Boolean>,
        val fullScreen: State<Boolean>,
        val textToSpeech: TextToSpeechSettingData,
        val liveTranslation: LiveTranslationSettingData,
        val style: StyleSettingsData,
        val selectedSetting: MutableState<Type>,
    ) {
        data class StyleSettingsData(
            val followSystem: State<Boolean>,
            val currentTheme: State<Themes>,
            val textFont: State<String>,
            val textSize: State<Float>,
        )

        enum class Type {
            None, LiveTranslation, TextToSpeech, Style, More
        }
    }
}