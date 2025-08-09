package com.android.scholar.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.scholar.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScholarOverlay(viewModel: ScholarViewModel = viewModel()) {
    val connectionState by viewModel.connectionState.collectAsState()
    val learningResponse by viewModel.learningResponse.collectAsState()
    val audioState by viewModel.audioState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val expandedQuestionIndex by viewModel.expandedQuestionIndex.collectAsState()
    
    var showServerConfig by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    
    // Auto-connect on first launch
    LaunchedEffect(Unit) {
        viewModel.connectToServer()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1421),
                        Color(0xFF1A2332),
                        Color(0xFF0D1421)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar with settings
            TopAppBar(
                title = {
                    Text(
                        text = "ScholAR",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    ServerConfigButton(
                        onClick = { showServerConfig = true }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            
            // Connection status
            ConnectionStatusCard(
                connectionState = connectionState,
                onConnect = { viewModel.connectToServer() },
                onClearError = { viewModel.clearConnectionError() }
            )
            
            // Main content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    learningResponse != null -> {
                        // Show learning response
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            item {
                                LearningResponseCard(
                                    response = learningResponse!!,
                                    audioState = audioState,
                                    expandedQuestionIndex = expandedQuestionIndex,
                                    onPlayAudio = {
                                        viewModel.playTtsAudio("static/" + learningResponse!!.ttsUrl)
                                    },
                                    onPauseAudio = {
                                        viewModel.pauseAudio()
                                    },
                                    onToggleQuestion = { index ->
                                        viewModel.toggleQuestionExpansion(index)
                                    }
                                )
                            }
                        }
                        
                        // Clear button
                        FloatingActionButton(
                            onClick = {
                                viewModel.clearLearningResponse()
                                viewModel.stopAudio()
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            containerColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Text(
                                text = "Clear",
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    isLoading || connectionState.isConnecting -> {
                        // Loading state
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 4.dp
                            )
                            Text(
                                text = if (connectionState.isConnecting) "Connecting to server..." else "Waiting for response...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    connectionState.isConnected -> {
                        // Connected and waiting
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "🎓 Ready to Learn!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(24.dp)
                                )
                            }
                            
                            Text(
                                text = "Ask a question using your ESP32 device\nand get instant learning responses",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                    
                    else -> {
                        // Not connected
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "📱 Welcome to ScholAR",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "Connect to your ScholAR server to start learning",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            
                            Button(
                                onClick = { showServerConfig = true },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Configure Server")
                            }
                        }
                    }
                }
            }
        }
        
        // Server configuration dialog
        ServerConfigDialog(
            isVisible = showServerConfig,
            onDismiss = { showServerConfig = false },
            onConnect = { host, port ->
                viewModel.connectToServer(host, port)
            }
        )
    }
}