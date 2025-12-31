package com.novitaai.studio.presentation.generate

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.novitaai.studio.domain.model.AiModel
import com.novitaai.studio.domain.model.GenerationResult
import com.novitaai.studio.domain.model.GenerationType
import com.novitaai.studio.domain.model.TaskStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateScreen(
    viewModel: GenerateViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate") },
                actions = {
                    IconButton(onClick = { /* Toggle dark mode */ }) {
                        Icon(Icons.Default.Palette, contentDescription = "Theme")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Generation Type Selector
            GenerationTypeSelector(
                selectedType = uiState.generationType,
                onTypeSelected = viewModel::updateGenerationType
            )

            // Prompt Input
            PromptInputSection(
                prompt = uiState.prompt,
                negativePrompt = uiState.negativePrompt,
                onPromptChange = viewModel::updatePrompt,
                onNegativePromptChange = viewModel::updateNegativePrompt
            )

            // Model Selector
            ModelSelectorSection(
                selectedModel = uiState.selectedModel,
                onModelClick = viewModel::toggleModelSelector,
                modifier = Modifier.fillMaxWidth()
            )

            // Advanced Settings Toggle
            AdvancedSettingsToggle(
                isExpanded = uiState.showAdvancedSettings,
                onToggle = viewModel::toggleAdvancedSettings
            )

            // Advanced Settings
            AnimatedVisibility(visible = uiState.showAdvancedSettings) {
                AdvancedSettingsSection(
                    width = uiState.width,
                    height = uiState.height,
                    steps = uiState.steps,
                    cfgScale = uiState.cfgScale,
                    onWidthChange = viewModel::updateWidth,
                    onHeightChange = viewModel::updateHeight,
                    onStepsChange = viewModel::updateSteps,
                    onCfgScaleChange = viewModel::updateCfgScale
                )
            }

            // Generate Button
            GenerateButton(
                isGenerating = uiState.isGenerating,
                progress = uiState.progress,
                onGenerate = viewModel::generate,
                enabled = uiState.prompt.isNotBlank()
            )

            // Error Message
            uiState.error?.let { error ->
                ErrorCard(error = error, onDismiss = viewModel::clearError)
            }

            // Result Preview
            uiState.currentResult?.let { result ->
                ResultCard(
                    result = result,
                    onDismiss = viewModel::clearResult
                )
            }
        }
    }
}

@Composable
fun GenerationTypeSelector(
    selectedType: GenerationType,
    onTypeSelected: (GenerationType) -> Unit
) {
    val types = listOf(
        GenerationType.TEXT_TO_IMAGE to "Image",
        GenerationType.TEXT_TO_VIDEO to "Video"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        types.forEach { (type, label) ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(label) },
                leadingIcon = if (selectedType == type) {
                    { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                } else null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PromptInputSection(
    prompt: String,
    negativePrompt: String,
    onPromptChange: (String) -> Unit,
    onNegativePromptChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            label = { Text("Prompt") },
            placeholder = { Text("Describe what you want to generate...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6
        )

        OutlinedTextField(
            value = negativePrompt,
            onValueChange = onNegativePromptChange,
            label = { Text("Negative Prompt (optional)") },
            placeholder = { Text("Things to avoid in the generation...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
    }
}

@Composable
fun ModelSelectorSection(
    selectedModel: AiModel?,
    onModelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onModelClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Model",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedModel?.displayName ?: "Select a model",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "Select model",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AdvancedSettingsToggle(
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    TextButton(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (isExpanded) "Hide Advanced Settings" else "Show Advanced Settings")
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null
        )
    }
}

@Composable
fun AdvancedSettingsSection(
    width: Int,
    height: Int,
    steps: Int,
    cfgScale: Float,
    onWidthChange: (Int) -> Unit,
    onHeightChange: (Int) -> Unit,
    onStepsChange: (Int) -> Unit,
    onCfgScaleChange: (Float) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dimensions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DimensionSlider(
                    label = "Width",
                    value = width,
                    onValueChange = onWidthChange,
                    range = 256..1024,
                    modifier = Modifier.weight(1f)
                )
                DimensionSlider(
                    label = "Height",
                    value = height,
                    onValueChange = onHeightChange,
                    range = 256..1024,
                    modifier = Modifier.weight(1f)
                )
            }

            // Steps
            ParameterSlider(
                label = "Steps",
                value = steps,
                onValueChange = onStepsChange,
                range = 1..50,
                unit = ""
            )

            // CFG Scale
            ParameterSlider(
                label = "CFG Scale",
                value = cfgScale,
                onValueChange = onCfgScaleChange,
                range = 1f..15f,
                unit = ""
            )
        }
    }
}

@Composable
fun DimensionSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("$value px", style = MaterialTheme.typography.bodySmall)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = (range.last - range.first) / 64
        )
    }
}

@Composable
fun ParameterSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    unit: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(String.format("%.1f %s", value, unit), style = MaterialTheme.typography.bodySmall)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range
        )
    }
}

@Composable
fun GenerateButton(
    isGenerating: Boolean,
    progress: Float,
    onGenerate: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onGenerate,
        enabled = enabled && !isGenerating,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        if (isGenerating) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generating...")
        } else {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate")
        }
    }
}

@Composable
fun ErrorCard(
    error: String,
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
                text = error,
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
fun ResultCard(
    result: GenerationResult,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Image/Video Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                result.imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Generated image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                result.videoUrl?.let { url ->
                    // Video placeholder
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.VideoLibrary,
                            contentDescription = "Video generated",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = when (result.status) {
                        TaskStatus.SUCCESS -> MaterialTheme.colorScheme.primary
                        TaskStatus.FAILED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.secondary
                    }
                ) {
                    Text(
                        text = result.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { /* Save */ }) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save")
                }
                TextButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dismiss")
                }
            }
        }
    }
}
