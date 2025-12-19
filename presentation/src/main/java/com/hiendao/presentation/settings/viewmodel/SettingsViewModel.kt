package com.hiendao.presentation.settings.viewmodel


import android.content.Context
import android.text.format.Formatter
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.hiendao.coreui.BaseViewModel
import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.coreui.theme.Themes
import com.hiendao.coreui.theme.toPreferenceTheme
import com.hiendao.coreui.theme.toTheme
import com.hiendao.coreui.utils.Toasty
import com.hiendao.data.utils.AppCoroutineScope
import com.hiendao.domain.repository.AppRepository
import com.hiendao.domain.translator.TranslationManager
import com.hiendao.domain.utils.AppFileResolver
import com.hiendao.presentation.settings.state.SettingsScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val appScope: AppCoroutineScope,
    private val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context,
    private val translationManager: TranslationManager,
    private val appFileResolver: AppFileResolver
) : BaseViewModel() {

    private val themeId by appPreferences.THEME_ID.state(viewModelScope)

    val state = SettingsScreenState(
        followsSystemTheme = appPreferences.THEME_FOLLOW_SYSTEM.state(viewModelScope),
        currentTheme = derivedStateOf { themeId.toTheme },
        currentLanguage = appPreferences.APP_LANGUAGE.state(viewModelScope),
        isTranslationSettingsVisible = mutableStateOf(translationManager.available),
        translationModelsStates = translationManager.models
    )

    init {

    }

    fun downloadTranslationModel(lang: String) {
        translationManager.downloadModel(lang)
    }

    fun removeTranslationModel(lang: String) {
        translationManager.removeModel(lang)
    }

    fun cleanImagesFolder() = appScope.launch(Dispatchers.IO) {
        val libraryFolders = appRepository.libraryBooks.getAllInLibrary()
            .asSequence()
            .map { appFileResolver.getLocalBookFolderName(it.id) }
            .toSet()

        appRepository.settings.folderBooks.listFiles()
            ?.asSequence()
            ?.filter { it.isDirectory && it.exists() }
            ?.filter { it.name !in libraryFolders }
            ?.forEach { it.deleteRecursively() }
        Glide.get(context).clearDiskCache()
    }

    fun onFollowSystemChange(follow: Boolean) {
        appPreferences.THEME_FOLLOW_SYSTEM.value = follow
    }

    fun onThemeChange(themes: Themes) {
        appPreferences.THEME_ID.value = themes.toPreferenceTheme
    }

    fun onLanguageChange(lang: String) {
        appPreferences.APP_LANGUAGE.value = lang
    }
}

private suspend fun getFolderSizeBytes(file: File): Long = withContext(Dispatchers.IO) {
    when {
        !file.exists() -> 0
        file.isFile -> file.length()
        else -> file.walkBottomUp().sumOf { if (it.isDirectory) 0 else it.length() }
    }
}


