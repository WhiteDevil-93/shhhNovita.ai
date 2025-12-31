package com.novitaai.studio.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novitaai.studio.domain.model.HistoryItem
import com.novitaai.studio.domain.usecase.DeleteHistoryItemUseCase
import com.novitaai.studio.domain.usecase.ClearHistoryUseCase
import com.novitaai.studio.domain.usecase.GetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the History screen
 */
data class HistoryUiState(
    val items: List<HistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = true,
    val selectedFilter: HistoryFilter = HistoryFilter.ALL,
    val error: String? = null
)

/**
 * Filter options for history
 */
enum class HistoryFilter {
    ALL,
    IMAGES,
    VIDEOS
}

/**
 * ViewModel for the History screen
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val deleteHistoryItemUseCase: DeleteHistoryItemUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            getHistoryUseCase().collect { items ->
                _uiState.update { state ->
                    state.copy(
                        items = items,
                        isLoading = false,
                        isEmpty = items.isEmpty()
                    )
                }
            }
        }
    }

    fun updateFilter(filter: HistoryFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            try {
                deleteHistoryItemUseCase(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                clearHistoryUseCase()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Get filtered items based on current filter
     */
    fun getFilteredItems(): List<HistoryItem> {
        val state = _uiState.value
        return when (state.selectedFilter) {
            HistoryFilter.ALL -> state.items
            HistoryFilter.IMAGES -> state.items.filter {
                it.type.name.contains("IMAGE", ignoreCase = true)
            }
            HistoryFilter.VIDEOS -> state.items.filter {
                it.type.name.contains("VIDEO", ignoreCase = true)
            }
        }
    }
}
