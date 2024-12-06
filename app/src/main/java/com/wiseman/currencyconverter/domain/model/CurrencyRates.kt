package com.wiseman.currencyconverter.domain.model

data class CurrencyRates(
    val baseCurrency: String?,
    val exchangeRates: Map<String, Double?>
)
