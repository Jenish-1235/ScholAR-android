package com.android.scholar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.scholar.ui.components.ScholarOverlay
import com.android.scholar.ui.theme.ScholARTheme
import com.android.scholar.viewmodel.ScholarViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ScholarViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScholARTheme {
                ScholarOverlay(viewModel)
            }
        }
    }
}