package com.wiseman.currencyconverter.presentation

sealed class UiEvent {
    data class CalculateTotalValue(val amount: Double) : UiEvent()
    data class CalculateCommission(val totalValue: Double) : UiEvent()
    data class ChangeSellingCurrency(val currencyCode: String) : UiEvent()
    data class ChangeBuyingCurrency(val currencyCode: String, val exchangeRate: Double) : UiEvent()
    data class UpdateAmountToBuy(val amountToSell: Double) : UiEvent()
    data class UpdateExchangeRate(val currency: String):UiEvent()
    data class PerformExchange(val buyingCurrencyCode: String, val buyingCurrencyAmount: Double, val sellingCurrencyAmount:Double) : UiEvent()
}