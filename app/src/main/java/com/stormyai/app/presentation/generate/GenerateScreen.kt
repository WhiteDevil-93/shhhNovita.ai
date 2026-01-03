package com.stormyai.app.presentation.generate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun GenerateRoute(
    paddingValues: PaddingValues,
    viewModel: GenerateViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    GenerateScreen(
        paddingValues = paddingValues,
        state = state,
        onPromptChange = viewModel::updatePrompt,
        onNegativePromptChange = viewModel::updateNegativePrompt,
        onWidthChange = { value -> value.toIntOrNull()?.let(viewModel::updateWidth) },
        onHeightChange = { value -> value.toIntOrNull()?.let(viewModel::updateHeight) },
        onStepsChange = { value -> value.toIntOrNull()?.let(viewModel::updateSteps) },
        onCfgScaleChange = { value -> value.toFloatOrNull()?.let(viewModel::updateCfgScale) },
        onModelSelected = viewModel::updateSelectedModel,
        onGenerate = viewModel::generate,
        onLoraNameChange = viewModel::updateLoraName,
        onLoraWeightChange = viewModel::updateLoraWeight,
        onAddLora = viewModel::addLora,
        onRemoveLora = viewModel::removeLora
    )
}

@Composable
private fun GenerateScreen(
    paddingValues: PaddingValues,
    state: GenerateUiState,
    onPromptChange: (String) -> Unit,
    onNegativePromptChange: (String) -> Unit,
    onWidthChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onStepsChange: (String) -> Unit,
    onCfgScaleChange: (String) -> Unit,
    onModelSelected: (com.stormyai.app.domain.model.AiModel) -> Unit,
    onGenerate: () -> Unit,
    onLoraNameChange: (String) -> Unit,
    onLoraWeightChange: (String) -> Unit,
    onAddLora: () -> Unit,
    onRemoveLora: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = state.prompt,
            onValueChange = onPromptChange,
            label = { Text("Prompt") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors()
        )

        OutlinedTextField(
            value = state.negativePrompt,
            onValueChange = onNegativePromptChange,
            label = { Text("Negative Prompt") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Model")
        if (state.models.isEmpty()) {
            Text("No models available.")
        } else {
            state.models.forEach { model ->
                Button(
                    onClick = { onModelSelected(model) },
                    enabled = state.selectedModel?.id != model.id
                ) {
                    Text(model.name)
                }
            }
        }

        Text("Advanced Settings")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.width.toString(),
                onValueChange = onWidthChange,
                label = { Text("Width") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.height.toString(),
                onValueChange = onHeightChange,
                label = { Text("Height") },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.steps.toString(),
                onValueChange = onStepsChange,
                label = { Text("Steps") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.cfgScale.toString(),
                onValueChange = onCfgScaleChange,
                label = { Text("CFG Scale") },
                modifier = Modifier.weight(1f)
            )
        }

        Text("LoRAs")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.loraName,
                onValueChange = onLoraNameChange,
                label = { Text("LoRA Name") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.loraWeight,
                onValueChange = onLoraWeightChange,
                label = { Text("Weight (0-1)") },
                modifier = Modifier.weight(0.6f)
            )
        }
        Button(onClick = onAddLora) {
            Text("Add LoRA")
        }
        if (state.loras.isEmpty()) {
            Text("No LoRAs added.")
        } else {
            state.loras.forEachIndexed { index, lora ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${lora.name} (${lora.weight})")
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onRemoveLora(index) }) {
                        Text("Remove")
                    }
                }
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator()
        }

        state.error?.let { error ->
            Text(text = error)
        }

        Button(
            onClick = onGenerate,
            enabled = !state.isGenerating
        ) {
            Text(if (state.isGenerating) "Generating..." else "Generate")
        }
    }
}
