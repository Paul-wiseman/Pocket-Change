package com.wiseman.currencyconverter.presentation.intent

/**
 * Represents events that can be triggered from the UI.
 *
 * This sealed class defines a set of events that can be triggered by user interactions
 * in the UI. Each event represents a specific action or change that needs to be
 * handled by the ViewModel.
 */
sealed class ExchangeRateIntent {
    data class CalculateTotalValue(val sellingCurrencyAmount: Double) : ExchangeRateIntent()
    data class CalculateCommission(val totalAmount: Double) : ExchangeRateIntent()
    data class ChangeSellingCurrency(val currencyCode: String) : ExchangeRateIntent()
    data class ChangeBuyingCurrency(val currencyCode: String) : ExchangeRateIntent()
    data class UpdateAmountToBuy(
        val sellingCurrencyCode: String,
        val buyingCurrencyCode: String,
        val sellingCurrencyAmount: Double
    ) : ExchangeRateIntent()

    data class PerformExchange(
        val sellingCurrencyCode: String,
        val buyingCurrencyCode: String,
        val sellingCurrencyAmount: Double
    ) : ExchangeRateIntent()
}

