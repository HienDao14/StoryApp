package com.hiendao.presentation.story.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiendao.data.remote.retrofit.story.model.CreateStoryRequest
import com.hiendao.domain.repository.StoryRepository
import com.hiendao.domain.utils.Response
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

    fun onLanguageChange(language: String) {
        _uiState.update { it.copy(language = language) }
    }

    fun generateStory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val state = uiState.value
            val request = CreateStoryRequest(
                title = state.title,
                freeText = state.freeText,
                durationSeconds = state.durationSeconds,
                language = state.language,
            )

            storyRepository.createStory(request).collect { response ->
                when(response) {
                    is Response.Loading -> {

                    }
                    is Response.Success -> {
                        _uiState.update { it.copy(isLoading = false, successMessage = "Story generated successfully!") }
                    }
                    is Response.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = response.message) } }
                    else -> Unit
                }
            }
        }
    }
}

data class CreateStoryUiState(
    val title: String = "",
    val freeText: String = "",
    val durationSeconds: Int = 300,
    val language: String = "vi",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
