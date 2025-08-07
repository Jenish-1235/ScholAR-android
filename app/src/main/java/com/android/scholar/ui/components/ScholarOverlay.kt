package com.android.scholar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.scholar.viewmodel.ScholarViewModel

@Composable
fun ScholarOverlay (viewModel: ScholarViewModel) {
    val answerText by viewModel.answerText.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ){
        if (answerText.isNotBlank()){
            Text(
                text = answerText,
                fontSize = 20.sp,
                color = Color.White
            )
        }else{
            Text(
                text = "Awaiting Command...",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }

}