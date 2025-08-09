package com.android.scholar.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.scholar.model.ConnectionState

@Composable
fun ConnectionStatusCard(
    connectionState: ConnectionState,
    onConnect: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = when {
            connectionState.isConnected -> Color(0xFF4CAF50)
            connectionState.isConnecting -> Color(0xFFFFC107)
            connectionState.error != null -> Color(0xFFF44336)
            else -> Color(0xFF9E9E9E)
        },
        animationSpec = tween(300), label = ""
    )
    
    val rotationAngle by animateFloatAsState(
        targetValue = if (connectionState.isConnecting) 360f else 0f,
        animationSpec = tween(1000), label = ""
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = when {
                            connectionState.isConnected -> "Connected"
                            connectionState.isConnecting -> "Connecting..."
                            connectionState.error != null -> "Connection Error"
                            else -> "Not Connected"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (connectionState.reconnectAttempts > 0) {
                        Text(
                            text = "Attempt ${connectionState.reconnectAttempts}/5",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Action button
            when {
                connectionState.isConnecting -> {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Connecting",
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotationAngle),
                        tint = statusColor
                    )
                }
                connectionState.isConnected -> {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Connected",
                        tint = statusColor
                    )
                }
                else -> {
                    IconButton(
                        onClick = {
                            if (connectionState.error != null) {
                                onClearError()
                            }
                            onConnect()
                        }
                    ) {
                        Icon(
                            imageVector = if (connectionState.error != null) Icons.Default.Refresh else Icons.Default.Refresh,
                            contentDescription = "Connect",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        // Error message
        if (connectionState.error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onClearError() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = connectionState.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
