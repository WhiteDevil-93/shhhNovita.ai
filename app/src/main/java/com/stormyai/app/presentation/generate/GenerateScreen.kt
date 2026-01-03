package com.stormyai.app.presentation.generate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stormyai.app.domain.model.ModelProfile

private val presetPrompts = listOf(
    "Realistic",
    "Cinematic",
    "NSFW Realism",
    "Portrait",
    "Product"
)

private val samplerOptions = listOf(
    "DPM++ SDE Karras",
    "DPM++ 2M Karras",
    "Euler a",
    "Euler",
    "DDIM"
)

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
        onWidthChange = { value ->
            if (value.isNotEmpty() && value.all { it.isDigit() }) {
                viewModel.updateWidth(value.toInt())
            }
        },
        onHeightChange = { value ->
            if (value.isNotEmpty() && value.all { it.isDigit() }) {
                viewModel.updateHeight(value.toInt())
            }
        },
        onStepsChange = { value ->
            if (value.isNotEmpty() && value.all { it.isDigit() }) {
                viewModel.updateSteps(value.toInt())
            }
        },
        onCfgScaleChange = { value ->
            val sanitized = value.replace(',', '.')
            val isValidFloat = sanitized.isNotEmpty() &&
                sanitized.count { it == '.' } <= 1 &&
                sanitized.all { it.isDigit() || it == '.' }
            if (isValidFloat) {
                viewModel.updateCfgScale(sanitized.toFloat())
            }
        },
        onSamplerChange = viewModel::updateSampler,
        onImageCountChange = viewModel::updateImageCount,
        onSeedModeChange = viewModel::updateSeedMode,
        onSeedChange = viewModel::updateSeed,
        onHighResFixChange = viewModel::updateHighResFix,
        onFaceRestoreChange = viewModel::updateFaceRestore,
        onNsfwChange = viewModel::updateNsfw,
        onProfileSelected = viewModel::updateSelectedProfile,
        onGenerate = viewModel::generate,
        onLoraNameChange = viewModel::updateLoraName,
        onLoraWeightChange = viewModel::updateLoraWeight,
        onAddLora = viewModel::addLora,
        onRemoveLora = viewModel::removeLora,
        onPresetPrompt = viewModel::updatePrompt
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
    onSamplerChange: (String) -> Unit,
    onImageCountChange: (Int) -> Unit,
    onSeedModeChange: (SeedMode) -> Unit,
    onSeedChange: (String) -> Unit,
    onHighResFixChange: (Boolean) -> Unit,
    onFaceRestoreChange: (Boolean) -> Unit,
    onNsfwChange: (Boolean) -> Unit,
    onProfileSelected: (ModelProfile) -> Unit,
    onGenerate: () -> Unit,
    onLoraNameChange: (String) -> Unit,
    onLoraWeightChange: (String) -> Unit,
    onAddLora: () -> Unit,
    onRemoveLora: (Int) -> Unit,
    onPresetPrompt: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatusIndicator(state)

        Text("Preset Prompts")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetPrompts.forEach { preset ->
                Button(onClick = { onPresetPrompt(preset) }) {
                    Text(preset)
                }
            }
        }

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

        Text("Model Profile")
        ProfileDropdown(
            profiles = state.profiles,
            selectedProfile = state.selectedProfile,
            onProfileSelected = onProfileSelected
        )

        Text("Sampler")
        DropdownSelector(
            options = samplerOptions,
            selected = state.sampler,
            onSelected = onSamplerChange
        )

        Text("Advanced Settings")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.width.toString(),
                onValueChange = onWidthChange,
                label = { Text("Width") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = state.height.toString(),
                onValueChange = onHeightChange,
                label = { Text("Height") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Text("Steps: ${state.steps}")
        Slider(
            value = state.steps.toFloat(),
            onValueChange = { onStepsChange(it.toInt().toString()) },
            valueRange = 1f..150f,
            steps = 148
        )
        Text("CFG Scale: ${state.cfgScale}")
        Slider(
            value = state.cfgScale,
            onValueChange = { onCfgScaleChange(it.toString()) },
            valueRange = 1f..20f
        )

        Text("Image Count: ${state.imageCount}")
        Slider(
            value = state.imageCount.toFloat(),
            onValueChange = { onImageCountChange(it.toInt()) },
            valueRange = 1f..8f,
            steps = 6
        )

        Text("Seed")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { onSeedModeChange(SeedMode.RANDOM) }) {
                Text("Random")
            }
            Button(onClick = { onSeedModeChange(SeedMode.FIXED) }) {
                Text("Fixed")
            }
        }
        if (state.seedMode == SeedMode.FIXED) {
            OutlinedTextField(
                value = state.seed,
                onValueChange = onSeedChange,
                label = { Text("Seed") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        ToggleRow("High-Res Fix", state.highResFix, onHighResFixChange)
        ToggleRow("Face Restore", state.faceRestore, onFaceRestoreChange)
        ToggleRow("NSFW Mode", state.nsfw, onNsfwChange)

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
                modifier = Modifier.weight(0.6f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
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

@Composable
private fun StatusIndicator(state: GenerateUiState) {
    val statusText = when (state.status) {
        GenerationStatus.IDLE -> "Idle"
        GenerationStatus.QUEUED -> "Queued"
        GenerationStatus.GENERATING -> "Generating"
        GenerationStatus.UPSCALING -> "Upscaling"
        GenerationStatus.DONE -> "Done"
        GenerationStatus.FAILED -> "Failed"
    }
    Text("Status: $statusText")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileDropdown(
    profiles: List<ModelProfile>,
    selectedProfile: ModelProfile?,
    onProfileSelected: (ModelProfile) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = selectedProfile?.name ?: "Select profile"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Profile") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            profiles.forEach { profile ->
                DropdownMenuItem(
                    text = { Text(profile.name) },
                    onClick = {
                        expanded = false
                        onProfileSelected(profile)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Sampler") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
