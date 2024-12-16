package com.wiseman.currencyconverter.presentation.state

import com.wiseman.currencyconverter.util.Constants.ZERO

/**
 * Data class representing currency exchange information.
 *
 * @property sellingCurrency The currency being sold. Defaults to EUR with amount 0.
 * @property buyingCurrency The currency being bought. Defaults to USD with amount 0.
 * @property commission The commission applied to the exchange. Defaults to 0.
 * @property totalAmount The total amount of the transaction in the selling currency. Defaults to 0.
 */
data class CurrencyExchangeData(
    val sellingCurrency: Currency = Currency("EUR", ZERO),
    val buyingCurrency: Currency = Currency("USD", ZERO),
    val commission: Double = ZERO,
    val totalAmount: Double = ZERO,
)

data class Currency(val code: String, val value: Double)
