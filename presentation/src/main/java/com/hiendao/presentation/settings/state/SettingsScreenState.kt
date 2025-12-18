package com.hiendao.presentation.settings.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.hiendao.coreui.theme.Themes
import com.hiendao.domain.translator.TranslationModelState

data class SettingsScreenState(
    val followsSystemTheme: State<Boolean>,
    val currentTheme: State<Themes>,
    val isTranslationSettingsVisible: State<Boolean>,
    val translationModelsStates: SnapshotStateList<TranslationModelState>
)