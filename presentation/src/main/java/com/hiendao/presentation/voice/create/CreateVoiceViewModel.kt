package com.hiendao.presentation.voice.create

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.coreui.utils.StringUtils
import com.hiendao.domain.model.CreateVoiceRequest
import com.hiendao.domain.repository.VoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.hiendao.coreui.R as CoreR

private const val MAX_RECORDING_DURATION = 300 // 5 minutes

@HiltViewModel
class CreateVoiceViewModel @Inject constructor(
    private val voiceGenerationManager: VoiceGenerationManager,
    private val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateVoiceUiState())
    val uiState = _uiState.asStateFlow()

    private val wavRecorder = WavAudioRecorder()
    private var mediaPlayer: MediaPlayer? = null
    private var audioFile: File? = null
    private var timerJob: Job? = null
    private var playbackJob: Job? = null

    init {
        // Observe persistent generation state
        viewModelScope.launch {
            voiceGenerationManager.isGenerating.collect { isGenerating ->
                _uiState.update { it.copy(isFeatureDisabled = isGenerating, isLoading = isGenerating) }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(voiceName = name) }
    }

    fun startRecording() {
        if (_uiState.value.isPlaying) {
            stopPlayback()
        }
        if (_uiState.value.isRecording) return
        
        val fileName = "record_${System.currentTimeMillis()}.wav"
        audioFile = File(context.cacheDir, fileName)

        try {
            wavRecorder.startRecording(audioFile!!)
            _uiState.update { it.copy(isRecording = true, errorMessage = null) }
            startTimer()
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Recording failed: ${e.message}") }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (true) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000
                _uiState.update { it.copy(recordingDuration = elapsed.toInt()) }
                
                if (elapsed >= MAX_RECORDING_DURATION) {
                    stopRecording()
                    _uiState.update { it.copy(errorMessage = "Recording limit reached (5 mins)") }
                    break
                }
                
                delay(1000)
            }
        }
    }

    fun stopRecording() {
        try {
            audioFile?.let { 
                wavRecorder.stopRecording(it)
                
                // Get duration immediately
                val mp = MediaPlayer()
                mp.setDataSource(it.absolutePath)
                mp.prepare()
                val duration = mp.duration
                mp.release()

                _uiState.update { state -> 
                    state.copy(
                        isRecording = false, 
                        recordedFile = audioFile,
                        playbackDuration = duration
                    ) 
                }
            } ?: run {
                 _uiState.update { it.copy(isRecording = false) }
            }
            timerJob?.cancel()
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Stop recording failed: ${e.message}") }
        }
    }

    fun playRecording() {
        if (_uiState.value.isPlaying) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        val file = audioFile ?: return
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    stopPlayback()
                }
            }
            val duration = mediaPlayer?.duration ?: 0
            _uiState.update { it.copy(isPlaying = true, playbackDuration = duration) }
            startPlaybackTimer()
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Playback failed: ${e.message}") }
        }
    }

    private fun startPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (true) {
                val currentPosition = mediaPlayer?.currentPosition ?: 0
                _uiState.update { it.copy(playbackPosition = currentPosition) }
                delay(100)
            }
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.release()
        mediaPlayer = null
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = false, playbackPosition = 0) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createVoice() {
        val state = _uiState.value
        if (state.voiceName.isBlank() || state.recordedFile == null) return
        if (state.isFeatureDisabled) return 

        _uiState.update { it.copy(errorMessage = null) }
        
        // Validation and Formatting
        val formattedName = StringUtils.removeAccents(state.voiceName).replace(" ", "")
        val userId = appPreferences.USER_ID.value
        val trainAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        
        val request = CreateVoiceRequest(
            name = formattedName,
            audioFile = state.recordedFile,
            userId = userId,
            trainAt = trainAt
        )
        
        voiceGenerationManager.createVoice(
            request = request,
            onLongRunning = {
                // If this callback runs, we are in the middle of generation (2s passed)
               _uiState.update { it.copy(showSuccessDialog = true) } 
               // Note: Reuse showSuccessDialog boolean to trigger "Dialog", but we need to handle content.
               // Currently Screen logic shows a specific success dialog? No, I need to check Screen logic.
               // Previously we planned: showLongProcessingData
            },
            onSuccess = {
                _uiState.update { 
                    it.copy(
                        successMessage = "Voice created successfully!", 
                        // If dialog is already showing (Processing), this update might change it?
                        // If dialog is NOT showing (fast response), we might want to show Success Dialog.
                        showSuccessDialog = true 
                    ) 
                }
            },
            onError = { msg ->
                _uiState.update { it.copy(errorMessage = msg) }
            }
        )
    }
    
    fun dismissSuccessDialog() {
         _uiState.update { it.copy(showSuccessDialog = false) }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        timerJob?.cancel()
        playbackJob?.cancel()
    }
}

data class CreateVoiceUiState(
    val voiceName: String = "",
    val isRecording: Boolean = false,
    val recordingDuration: Int = 0,
    val recordedFile: File? = null,
    val isPlaying: Boolean = false,
    val playbackPosition: Int = 0,
    val playbackDuration: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showSuccessDialog: Boolean = false,
    val isFeatureDisabled: Boolean = false
)
