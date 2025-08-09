package com.android.scholar.service

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.android.scholar.model.AudioState

class AudioPlayerManager(private val context: Context) {
    
    private var exoPlayer: ExoPlayer? = null
    
    private val _audioState = MutableStateFlow(AudioState())
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow()
    
    init {
        initializePlayer()
    }
    
    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_IDLE -> {
                            _audioState.value = _audioState.value.copy(isPlaying = false, isLoading = false)
                        }
                        Player.STATE_BUFFERING -> {
                            _audioState.value = _audioState.value.copy(isLoading = true)
                        }
                        Player.STATE_READY -> {
                            _audioState.value = _audioState.value.copy(
                                isPlaying = isPlaying,
                                isLoading = false,
                                error = null
                            )
                        }
                        Player.STATE_ENDED -> {
                            _audioState.value = _audioState.value.copy(isPlaying = false, isLoading = false)
                        }
                    }
                }
                
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    Log.e("AudioPlayer", "Playback error: ${error.message}")
                    _audioState.value = _audioState.value.copy(
                        isPlaying = false,
                        isLoading = false,
                        error = "Failed to play audio: ${error.message}"
                    )
                }
            })
        }
    }
    
    fun playTtsAudio(audioUrl: String) {
        try {
            _audioState.value = _audioState.value.copy(
                isLoading = true,
                error = null,
                currentUrl = audioUrl
            )
            
            val mediaItem = MediaItem.fromUri(audioUrl)
            exoPlayer?.apply {
                setMediaItem(mediaItem)
                prepare()
                play()
            }
            
            Log.d("AudioPlayer", "Playing TTS audio: $audioUrl")
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing TTS audio", e)
            _audioState.value = _audioState.value.copy(
                isPlaying = false,
                isLoading = false,
                error = "Failed to play audio: ${e.message}"
            )
        }
    }
    
    fun pauseAudio() {
        exoPlayer?.pause()
        _audioState.value = _audioState.value.copy(isPlaying = false)
    }
    
    fun resumeAudio() {
        exoPlayer?.play()
        _audioState.value = _audioState.value.copy(isPlaying = true)
    }
    
    fun stopAudio() {
        exoPlayer?.stop()
        _audioState.value = _audioState.value.copy(
            isPlaying = false,
            isLoading = false,
            currentUrl = null
        )
    }
    
    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        _audioState.value = AudioState()
    }
    
    fun clearError() {
        _audioState.value = _audioState.value.copy(error = null)
    }
}
