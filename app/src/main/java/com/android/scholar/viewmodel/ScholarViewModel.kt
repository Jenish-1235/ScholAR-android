package com.android.scholar.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.Text

class ScholarViewModel : ViewModel() {
    private val _answerText = MutableStateFlow("")
    val answerText = _answerText.asStateFlow()

    fun updateAnswer(newText: String){
        _answerText.value = newText
    }

    fun clearAnswer(){
        _answerText.value = ""
    }
}