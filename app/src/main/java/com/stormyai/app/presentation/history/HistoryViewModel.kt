package com.stormyai.app.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stormyai.app.domain.model.HistoryItem
import com.stormyai.app.domain.usecase.GetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase
) : ViewModel() {

    private val mutableItems = MutableStateFlow<List<HistoryItem>>(emptyList())
    val items: StateFlow<List<HistoryItem>> = mutableItems.asStateFlow()

    init {
        viewModelScope.launch {
            getHistoryUseCase().collect { history ->
                mutableItems.value = history
            }
        }
    }
}
