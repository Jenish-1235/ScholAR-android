package com.android.scholar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.scholar.network.NetworkConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConfigDialog(
    isVisible: Boolean,
    currentHost: String = NetworkConfig.DEFAULT_SERVER_HOST,
    currentPort: Int = NetworkConfig.DEFAULT_SERVER_PORT,
    onDismiss: () -> Unit,
    onConnect: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var host by remember(currentHost) { mutableStateOf(currentHost) }
    var portText by remember(currentPort) { mutableStateOf(currentPort.toString()) }
    var isHostError by remember { mutableStateOf(false) }
    var isPortError by remember { mutableStateOf(false) }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Server Configuration",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    
                    OutlinedTextField(
                        value = host,
                        onValueChange = { 
                            host = it
                            isHostError = it.isBlank()
                        },
                        label = { Text("Server Host") },
                        placeholder = { Text("10.10.30.172") },
                        isError = isHostError,
                        supportingText = if (isHostError) {
                            { Text("Host cannot be empty") }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    OutlinedTextField(
                        value = portText,
                        onValueChange = { 
                            portText = it
                            isPortError = it.toIntOrNull() == null || it.toIntOrNull()!! !in 1..65535
                        },
                        label = { Text("Server Port") },
                        placeholder = { Text("8000") },
                        isError = isPortError,
                        supportingText = if (isPortError) {
                            { Text("Port must be between 1 and 65535") }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        
                        Button(
                            onClick = {
                                val port = portText.toIntOrNull()
                                if (host.isNotBlank() && port != null && port in 1..65535) {
                                    onConnect(host, port)
                                    onDismiss()
                                } else {
                                    isHostError = host.isBlank()
                                    isPortError = port == null || port !in 1..65535
                                }
                            },
                            enabled = host.isNotBlank() && portText.toIntOrNull() != null
                        ) {
                            Text("Connect")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServerConfigButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Server Settings",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
