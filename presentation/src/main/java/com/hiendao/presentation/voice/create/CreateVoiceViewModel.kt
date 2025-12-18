package com.hiendao.presentation.voice.create

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CreateVoiceViewModel @Inject constructor(
    private val voiceRepository: VoiceRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateVoiceUiState())
    val uiState = _uiState.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFile: File? = null
    private var timerJob: Job? = null
    private var playbackJob: Job? = null

    fun onNameChange(name: String) {
        _uiState.update { it.copy(voiceName = name) }
    }

    fun startRecording() {
        if (_uiState.value.isRecording) return
        
        val fileName = "record_${System.currentTimeMillis()}.mp3"
        audioFile = File(context.cacheDir, fileName)

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile?.absolutePath)
            try {
                prepare()
                start()
                _uiState.update { it.copy(isRecording = true, errorMessage = null) }
                startTimer()
            } catch (e: IOException) {
                _uiState.update { it.copy(errorMessage = "Recording failed: ${e.message}") }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (true) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000
                _uiState.update { it.copy(recordingDuration = elapsed.toInt()) }
                delay(1000)
            }
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            timerJob?.cancel()
            _uiState.update { it.copy(isRecording = false, recordedFile = audioFile) }
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

    fun createVoice() {
        val state = _uiState.value
        if (state.voiceName.isBlank() || state.recordedFile == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val request = CreateVoiceRequest(state.voiceName, state.recordedFile)
            val result = voiceRepository.createVoice(request)
            
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Voice created successfully!") }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
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
    val successMessage: String? = null
)
