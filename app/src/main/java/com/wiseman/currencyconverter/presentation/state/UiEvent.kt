package com.wiseman.currencyconverter.presentation.state

/**
 * Represents events that can be triggered from the UI.
 *
 * This sealed class defines a set of events that can be triggered by user interactions
 * in the UI. Each event represents a specific action or change that needs to be
 * handled by the ViewModel.
 */
sealed class UiEvent {
    data class CalculateTotalValue(val sellingCurrencyAmount: Double) : UiEvent()
    data class CalculateCommission(val totalAmount: Double) : UiEvent()
    data class ChangeSellingCurrency(val currencyCode: String) : UiEvent()
    data class ChangeBuyingCurrency(val currencyCode: String) : UiEvent()
    data class UpdateAmountToBuy(
        val sellingCurrencyCode: String,
        val buyingCurrencyCode: String,
        val sellingCurrencyAmount: Double
    ) : UiEvent()

    data class PerformExchange(
        val sellingCurrencyCode: String,
        val buyingCurrencyCode: String,
        val sellingCurrencyAmount: Double
    ) : UiEvent()
}

