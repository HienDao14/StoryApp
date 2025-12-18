package com.hiendao.presentation.story.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiendao.data.remote.retrofit.story.model.CreateStoryRequest
import com.hiendao.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateStoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateStoryUiState())
    val uiState: StateFlow<CreateStoryUiState> = _uiState.asStateFlow()

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onFreeTextChange(text: String) {
        _uiState.update { it.copy(freeText = text) }
    }

    fun onDurationChange(seconds: Int) {
        _uiState.update { it.copy(durationSeconds = seconds) }
    }

    fun onReadingLevelChange(level: String) {
        _uiState.update { it.copy(readingLevel = level) }
    }

    fun onGenreChange(genre: String) {
        _uiState.update { it.copy(genre = genre) }
    }

    fun onToneChange(tone: String) {
        _uiState.update { it.copy(tone = tone) }
    }

    fun onKeyMessageAdd(message: String) {
        if (message.isBlank()) return
        _uiState.update { it.copy(keyMessages = it.keyMessages + message) }
    }

    fun onKeyMessageRemove(index: Int) {
        _uiState.update {
            val newList = it.keyMessages.toMutableList().apply { removeAt(index) }
            it.copy(keyMessages = newList)
        }
    }

    fun onIncludeSoundCuesChange(include: Boolean) {
        _uiState.update { it.copy(includeSoundCues = include) }
    }

    fun onLanguageChange(language: String) {
        _uiState.update { it.copy(language = language) }
    }

    fun onAdditionalInstructionsChange(instructions: String) {
        _uiState.update { it.copy(additionalInstructions = instructions) }
    }

    fun onCharacterAdd(name: String, description: String) {
        if (name.isBlank()) return
        val newChar = CreateStoryRequest.Character(name, description)
        _uiState.update { it.copy(characters = it.characters + newChar) }
    }

    fun onCharacterRemove(index: Int) {
        _uiState.update {
            val newList = it.characters.toMutableList().apply { removeAt(index) }
            it.copy(characters = newList)
        }
    }

    fun generateStory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val state = uiState.value
            val request = CreateStoryRequest(
                title = state.title,
                freeText = state.freeText,
                durationSeconds = state.durationSeconds,
                readingLevel = state.readingLevel,
                genre = state.genre,
                characters = state.characters,
                tone = state.tone,
                keyMessages = state.keyMessages,
                includeSoundCues = state.includeSoundCues,
                language = state.language,
                additionalInstructions = state.additionalInstructions
            )

            val result = storyRepository.createStory(request)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Story generated successfully!") }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message ?: "Unknown error") }
            }
        }
    }
}

data class CreateStoryUiState(
    val title: String = "",
    val freeText: String = "",
    val durationSeconds: Int = 300,
    val readingLevel: String = "5-10",
    val genre: String = "",
    val characters: List<CreateStoryRequest.Character> = emptyList(),
    val tone: String = "",
    val keyMessages: List<String> = emptyList(),
    val includeSoundCues: Boolean = true,
    val language: String = "vi",
    val additionalInstructions: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
