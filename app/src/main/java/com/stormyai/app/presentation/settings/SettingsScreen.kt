package com.stormyai.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsRoute(
    paddingValues: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    SettingsScreen(
        paddingValues = paddingValues,
        state = state,
        onApiKeyChange = viewModel::updateApiKey,
        onModelChange = viewModel::updateDefaultModelId,
        onSamplerChange = viewModel::updateDefaultSampler,
        onWidthChange = viewModel::updateDefaultWidth,
        onHeightChange = viewModel::updateDefaultHeight,
        onStepsChange = viewModel::updateDefaultSteps,
        onCfgScaleChange = viewModel::updateDefaultCfgScale,
        onSaveHistoryChange = viewModel::updateSaveHistory,
        onSave = viewModel::saveSettings
    )
}

@Composable
private fun SettingsScreen(
    paddingValues: PaddingValues,
    state: SettingsUiState,
    onApiKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onSamplerChange: (String) -> Unit,
    onWidthChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onStepsChange: (String) -> Unit,
    onCfgScaleChange: (String) -> Unit,
    onSaveHistoryChange: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        OutlinedTextField(
            value = state.apiKey,
            onValueChange = onApiKeyChange,
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.defaultModelId,
            onValueChange = onModelChange,
            label = { Text("Default Model ID") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.defaultSampler,
            onValueChange = onSamplerChange,
            label = { Text("Default Sampler") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.defaultWidth,
            onValueChange = onWidthChange,
            label = { Text("Default Width") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = state.defaultHeight,
            onValueChange = onHeightChange,
            label = { Text("Default Height") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = state.defaultSteps,
            onValueChange = onStepsChange,
            label = { Text("Default Steps") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = state.defaultCfgScale,
            onValueChange = onCfgScaleChange,
            label = { Text("Default CFG Scale") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Column {
            Text("Save History")
            Checkbox(
                checked = state.saveHistory,
                onCheckedChange = onSaveHistoryChange
            )
        }
        
        state.validationMessage?.let { message ->
            Text(
                text = message,
                color = androidx.compose.ui.graphics.Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Button(onClick = onSave) {
            Text("Save Settings")
        }
    }
}
