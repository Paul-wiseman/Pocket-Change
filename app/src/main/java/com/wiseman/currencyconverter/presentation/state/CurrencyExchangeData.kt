package com.wiseman.currencyconverter.presentation.state

import com.wiseman.currencyconverter.util.Constants.ZERO

data class CurrencyExchangeData(
    val sellingCurrency: Currency = Currency("EUR", ZERO),
    val buyingCurrency: Currency = Currency("USD", ZERO),
    val commission: Double = ZERO,
    val totalAmount: Double = ZERO,
)

data class Currency(val code: String, val value: Double)
