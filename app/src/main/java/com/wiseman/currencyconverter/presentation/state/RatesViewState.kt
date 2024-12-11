package com.wiseman.currencyconverter.presentation.state

data class RatesViewState<T>(
    val data: T?= null,
    val uiState: UiState = UiState.Loading,
    val error: String? = null
)

sealed class UiState {
    data object Loading : UiState()
    data object Error : UiState()
    data object Success : UiState()
}
