package com.pr0ph0z.fgoaccountswitcher

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun updateDialog(state: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(showDialog = state)
        }
    }

    fun updateSelectedAccount(state: Int) {
        _uiState.update { currentState ->
            currentState.copy(selectedAccount = state)
        }
    }
}