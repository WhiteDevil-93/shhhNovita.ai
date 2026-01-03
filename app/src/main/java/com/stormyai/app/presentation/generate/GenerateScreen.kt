package com.stormyai.app.presentation.generate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
        onGenerate = viewModel::generate
    )
}

@Composable
private fun GenerateScreen(
    paddingValues: PaddingValues,
    state: GenerateUiState,
    onPromptChange: (String) -> Unit,
    onGenerate: () -> Unit
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
