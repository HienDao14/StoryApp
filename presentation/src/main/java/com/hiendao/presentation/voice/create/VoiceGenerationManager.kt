package com.hiendao.presentation.voice.create

import com.hiendao.data.utils.AppCoroutineScope
import com.hiendao.domain.model.CreateVoiceRequest
import com.hiendao.domain.repository.VoiceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceGenerationManager @Inject constructor(
    private val voiceRepository: VoiceRepository,
    private val appCoroutineScope: AppCoroutineScope
) {
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()
    
    // We can expose the last result if needed, but for now we just track "isGenerating"
    // and rely on callbacks for immediate feedback if screen is alive.

    fun createVoice(
        request: CreateVoiceRequest,
        onLongRunning: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (_isGenerating.value) return // Prevent duplicate calls

        _isGenerating.value = true

        appCoroutineScope.launch {
            val longRunningCheckJob = launch {
                delay(2000)
                if (_isGenerating.value) {
                    onLongRunning()
                }
            }
            
            val result = voiceRepository.createVoice(request)
            longRunningCheckJob.cancel()
            
            _isGenerating.value = false
            
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}
