package com.wiseman.currencyconverter.presentation

sealed class UiEvent {
    data class CalculateTotalValue(val amount: Double) : UiEvent()
    data class CalculateCommission(val totalValue: Double) : UiEvent()
    data class ChangeSellingCurrency(val currencyCode: String) : UiEvent()
    data class ChangeBuyingCurrency(val currencyCode: String, val exchangeRate: Double) : UiEvent()
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