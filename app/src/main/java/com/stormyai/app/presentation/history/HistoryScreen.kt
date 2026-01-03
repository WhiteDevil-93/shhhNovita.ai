package com.stormyai.app.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HistoryRoute(
    paddingValues: PaddingValues,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    HistoryScreen(paddingValues, items, viewModel::remix)
}

@Composable
private fun HistoryScreen(
    paddingValues: PaddingValues,
    items: List<com.stormyai.app.domain.model.HistoryItem>,
    onRemix: (com.stormyai.app.domain.model.HistoryItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        if (items.isEmpty()) {
            Text("No history yet.")
        } else {
            items.forEach { item ->
                Text("${item.prompt} (${item.type})")
                Text("Model: ${item.modelName} • Sampler: ${item.sampler}")
                Text("Steps: ${item.steps} • CFG: ${item.cfgScale} • Seed: ${item.seed ?: "Random"}")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = { onRemix(item) }) {
                        Text("Remix")
                    }
                }
            }
        }
    }
}
