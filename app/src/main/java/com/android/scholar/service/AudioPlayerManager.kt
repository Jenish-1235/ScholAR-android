package com.android.scholar.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.android.scholar.model.AudioState

class AudioPlayerManager(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    
    private val _audioState = MutableStateFlow(AudioState())
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow()
    
    init {
        setupAudioFocus()
    }
    
    private fun setupAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
                
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(false)
                .setOnAudioFocusChangeListener { focusChange ->
                    Log.d("AudioPlayer", "Audio focus changed: $focusChange")
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_GAIN -> {
                            Log.d("AudioPlayer", "Audio focus gained")
                            mediaPlayer?.setVolume(1.0f, 1.0f)
                        }
                        AudioManager.AUDIOFOCUS_LOSS -> {
                            Log.d("AudioPlayer", "Audio focus lost")
                            stopAudio()
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                            Log.d("AudioPlayer", "Audio focus lost transient")
                            pauseAudio()
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                            Log.d("AudioPlayer", "Audio focus lost transient can duck")
                            mediaPlayer?.setVolume(0.3f, 0.3f)
                        }
                    }
                }
                .build()
        }
    }
    
    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { request ->
                val result = audioManager.requestAudioFocus(request)
                Log.d("AudioPlayer", "Audio focus request result: $result")
                result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } ?: run {
                Log.w("AudioPlayer", "Audio focus request is null")
                false
            }
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager.requestAudioFocus(
                { focusChange ->
                    Log.d("AudioPlayer", "Audio focus changed (legacy): $focusChange")
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_GAIN -> mediaPlayer?.setVolume(1.0f, 1.0f)
                        AudioManager.AUDIOFOCUS_LOSS -> stopAudio()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pauseAudio()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> mediaPlayer?.setVolume(0.3f, 0.3f)
                    }
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            Log.d("AudioPlayer", "Audio focus request result (legacy): $result")
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }
    
    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { request ->
                audioManager.abandonAudioFocusRequest(request)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }
    
    fun playTtsAudio(audioUrl: String) {
        try {
            Log.d("AudioPlayer", "=== STARTING MEDIAPLAYEF AUDIO PLAYBACK ===")
            Log.d("AudioPlayer", "Audio URL: $audioUrl")
            
            // Check device volume
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            Log.d("AudioPlayer", "Device volume: $currentVolume/$maxVolume")
            
            if (currentVolume == 0) {
                Log.w("AudioPlayer", "Device volume is 0, setting to 50% of max")
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, 0)
            }
            
            // Release any existing MediaPlayer
            mediaPlayer?.release()
            
            _audioState.value = _audioState.value.copy(
                isLoading = true,
                error = null,
                currentUrl = audioUrl
            )
            
            // Try to request audio focus
            val audioFocusGranted = requestAudioFocus()
            Log.d("AudioPlayer", "Audio focus granted: $audioFocusGranted")
            
            // Create new MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                Log.d("AudioPlayer", "Creating MediaPlayer...")
                
                // Set audio attributes for speech
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                } else {
                    @Suppress("DEPRECATION")
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                }
                
                // Set listeners
                setOnPreparedListener { mp ->
                    Log.d("AudioPlayer", "MediaPlayer prepared, starting playback...")
                    _audioState.value = _audioState.value.copy(
                        isLoading = false,
                        isPlaying = true,
                        error = null
                    )
                    mp.start()
                    Log.d("AudioPlayer", "MediaPlayer started successfully!")
                }
                
                setOnCompletionListener { 
                    Log.d("AudioPlayer", "MediaPlayer playback completed")
                    _audioState.value = _audioState.value.copy(
                        isPlaying = false,
                        isLoading = false
                    )
                    releaseAudioFocus()
                }
                
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "MediaPlayer error: what=$what, extra=$extra")
                    _audioState.value = _audioState.value.copy(
                        isPlaying = false,
                        isLoading = false,
                        error = "MediaPlayer error: $what, $extra"
                    )
                    releaseAudioFocus()
                    true
                }
                
                // Set data source and prepare
                Log.d("AudioPlayer", "Setting data source: $audioUrl")
                setDataSource(audioUrl)
                
                Log.d("AudioPlayer", "Preparing MediaPlayer asynchronously...")
                prepareAsync()
            }
            
            Log.d("AudioPlayer", "=== MEDIAPLAYEF SETUP COMPLETE ===")
        } catch (e: Exception) {
            Log.e("AudioPlayer", "=== MEDIAPLAYEF SETUP FAILED ===", e)
            _audioState.value = _audioState.value.copy(
                isPlaying = false,
                isLoading = false,
                error = "Failed to setup MediaPlayer: ${e.message}"
            )
            releaseAudioFocus()
        }
    }
    
    fun pauseAudio() {
        try {
            Log.d("AudioPlayer", "Pausing MediaPlayer...")
            mediaPlayer?.pause()
            _audioState.value = _audioState.value.copy(isPlaying = false)
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error pausing audio", e)
        }
    }
    
    fun resumeAudio() {
        try {
            Log.d("AudioPlayer", "Resuming MediaPlayer...")
            mediaPlayer?.start()
            _audioState.value = _audioState.value.copy(isPlaying = true)
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error resuming audio", e)
        }
    }
    
    fun stopAudio() {
        try {
            Log.d("AudioPlayer", "Stopping MediaPlayer...")
            mediaPlayer?.stop()
            _audioState.value = _audioState.value.copy(
                isPlaying = false,
                isLoading = false,
                currentUrl = null
            )
            releaseAudioFocus()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error stopping audio", e)
        }
    }
    
    fun release() {
        try {
            Log.d("AudioPlayer", "Releasing MediaPlayer...")
            mediaPlayer?.release()
            mediaPlayer = null
            _audioState.value = AudioState()
            releaseAudioFocus()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error releasing MediaPlayer", e)
        }
    }
    
    fun clearError() {
        _audioState.value = _audioState.value.copy(error = null)
    }
    
    // Test method to verify audio URL accessibility
    fun testAudioUrl(audioUrl: String) {
        Log.d("AudioPlayer", "Testing audio URL with MediaPlayer: $audioUrl")
        try {
            val testPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                // Don't prepare or play, just test the data source
            }
            testPlayer.release()
            Log.d("AudioPlayer", "Audio URL test successful: $audioUrl")
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Audio URL test failed: $audioUrl", e)
        }
    }
}
