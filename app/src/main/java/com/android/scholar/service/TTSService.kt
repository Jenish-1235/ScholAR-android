package com.android.scholar.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log

class TTSService (context: Context) {

    private var tts : TextToSpeech? = null

    init {
        tts = TextToSpeech(context){
            if (it == TextToSpeech.SUCCESS) {
                // Set the language to US English
                val result = tts?.setLanguage(java.util.Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Handle the case where the language is not supported
                    Log.e("TTSService", "Language not supported or missing data")
                }
            } else {
                // Initialization failed
                Log.e("TTSService", "Initialization failed")
            }
        }
    }

    fun speak(text: String) {
        if (tts != null && text.isNotEmpty()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutDown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}