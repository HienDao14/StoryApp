package com.hiendao.presentation.reader.features

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiVoicePlayer @Inject constructor(
    @ApplicationContext val context: Context,
) {
    private var mediaPlayer: MediaPlayer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    fun play(url: String, onCompletion: () -> Unit = {}, onError: (String) -> Unit = {}) {
        android.util.Log.d("AiVoicePlayer", "Attempting to play URL: $url")
//        release()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            try {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { 
                    android.util.Log.d("AiVoicePlayer", "MediaPlayer prepared successfully")
                    start() 
                    _isPlaying.value = true
                }
                setOnCompletionListener { 
                    _isPlaying.value = false
                    onCompletion() 
                }
                setOnErrorListener { _, what, extra ->
                    android.util.Log.e("AiVoicePlayer", "MediaPlayer Error: what=$what, extra=$extra")
                    _isPlaying.value = false
                    onError("MediaPlayer Error: $what, $extra")
                    true
                }
            } catch (e: Exception) {
                android.util.Log.e("AiVoicePlayer", "Exception initializing MediaPlayer", e)
                _isPlaying.value = false
                onError(e.message ?: "Unknown MediaPlayer error")
            }
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
        _isPlaying.value = false
    }

    fun resume(): Boolean {
        if (mediaPlayer != null && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            _isPlaying.value = true
            return true
        }
        return false
    }

    fun stop() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
        }
        _isPlaying.value = false
    }

    fun release() {
        stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
