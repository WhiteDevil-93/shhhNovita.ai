package com.stormyai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.stormyai.app.presentation.navigation.StormyAiNavGraph
import com.stormyai.app.presentation.theme.StormyAiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StormyAiApp()
        }
    }
}

@Composable
private fun StormyAiApp() {
    StormyAiTheme(darkTheme = isSystemInDarkTheme()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            StormyAiNavGraph()
        }
    }
}
