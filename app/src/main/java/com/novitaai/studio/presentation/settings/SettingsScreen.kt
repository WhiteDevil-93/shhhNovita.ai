package com.novitaai.studio.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showApiKey by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // API Key Section
            SettingsSection(title = "API Configuration") {
                ApiKeyField(
                    apiKey = uiState.apiKey,
                    showApiKey = showApiKey,
                    isValid = uiState.isApiKeyValid,
                    isLoading = uiState.isLoading,
                    onApiKeyChange = viewModel::updateApiKey,
                    onToggleVisibility = { showApiKey = !showApiKey },
                    onValidate = viewModel::validateApiKey
                )
            }

            // Default Generation Settings
            SettingsSection(title = "Default Generation Settings") {
                DefaultModelField(
                    model = uiState.defaultModel,
                    onModelChange = viewModel::updateDefaultModel
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DefaultDimensionField(
                        label = "Default Width",
                        value = uiState.defaultWidth,
                        onValueChange = viewModel::updateDefaultWidth,
                        modifier = Modifier.weight(1f)
                    )
                    DefaultDimensionField(
                        label = "Default Height",
                        value = uiState.defaultHeight,
                        onValueChange = viewModel::updateDefaultHeight,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                DefaultStepsField(
                    steps = uiState.defaultSteps,
                    onStepsChange = viewModel::updateDefaultSteps
                )
            }

            // App Preferences
            SettingsSection(title = "App Preferences") {
                SettingsSwitch(
                    title = "Save History",
                    description = "Automatically save generations to history",
                    checked = uiState.saveHistory,
                    onCheckedChange = viewModel::updateSaveHistory,
                    icon = Icons.Default.History
                )

                SettingsSwitch(
                    title = "Auto Download",
                    description = "Automatically save images to gallery",
                    checked = uiState.autoDownload,
                    onCheckedChange = viewModel::updateAutoDownload,
                    icon = Icons.Default.Download
                )

                SettingsSwitch(
                    title = "Dark Mode",
                    description = "Use dark theme",
                    checked = uiState.darkMode,
                    onCheckedChange = viewModel::updateDarkMode,
                    icon = Icons.Default.DarkMode
                )
            }

            // About Section
            SettingsSection(title = "About") {
                Column {
                    SettingsRow(
                        title = "Version",
                        value = "1.0.0",
                        icon = Icons.Default.Info
                    )
                    SettingsRow(
                        title = "API",
                        value = "Novita.ai v3",
                        icon = Icons.Default.Api
                    )
                }
            }

            // Save Button
            Button(
                onClick = viewModel::saveSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Settings")
                }
            }

            // Messages
            uiState.error?.let { error ->
                ErrorMessage(message = error, onDismiss = viewModel::clearError)
            }

            uiState.successMessage?.let { message ->
                SuccessMessage(message = message, onDismiss = viewModel::clearSuccessMessage)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

@Composable
fun ApiKeyField(
    apiKey: String,
    showApiKey: Boolean,
    isValid: Boolean?,
    isLoading: Boolean,
    onApiKeyChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onValidate: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = apiKey,
            onValueChange = onApiKeyChange,
            label = { Text("Novita.ai API Key") },
            placeholder = { Text("Enter your API key") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showApiKey) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                Row {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            imageVector = if (showApiKey) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = "Toggle visibility"
                        )
                    }
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = onValidate) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Validate",
                                tint = when (isValid) {
                                    true -> MaterialTheme.colorScheme.primary
                                    false -> MaterialTheme.colorScheme.error
                                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            },
            supportingText = {
                Text(
                    when (isValid) {
                        true -> "API key is valid"
                        false -> "API key is invalid"
                        null -> "Enter your Novita.ai API key"
                    }
                )
            },
            isError = isValid == false
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { /* Open API key setup */ }) {
            Icon(Icons.Default.OpenInNew, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Get API Key from Novita.ai")
        }
    }
}

@Composable
fun DefaultModelField(
    model: String,
    onModelChange: (String) -> Unit
) {
    OutlinedTextField(
        value = model,
        onValueChange = onModelChange,
        label = { Text("Default Model") },
        placeholder = { Text("meinamix_v11") },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
        }
    )
}

@Composable
fun DefaultDimensionField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { it.toIntOrNull()?.let(onValueChange) },
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        suffix = { Text("px") }
    )
}

@Composable
fun DefaultStepsField(
    steps: Int,
    onStepsChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Default Steps", style = MaterialTheme.typography.bodyMedium)
            Text("$steps", style = MaterialTheme.typography.bodyMedium)
        }
        Slider(
            value = steps.toFloat(),
            onValueChange = { onStepsChange(it.toInt()) },
            valueRange = 1f..50f,
            steps = 48
        )
    }
}

@Composable
fun SettingsSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsRow(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    }
}

@Composable
fun SuccessMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    }
}
