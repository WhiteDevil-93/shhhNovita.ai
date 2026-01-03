package com.stormyai.app.common

import com.stormyai.app.domain.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object RemixStore {
    private val mutableItem = MutableStateFlow<HistoryItem?>(null)
    val item: StateFlow<HistoryItem?> = mutableItem

    fun set(item: HistoryItem) {
        mutableItem.value = item
    }

    fun clear() {
        mutableItem.value = null
    }
}
