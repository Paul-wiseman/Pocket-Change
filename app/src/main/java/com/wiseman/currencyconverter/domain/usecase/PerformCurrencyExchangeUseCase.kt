package com.wiseman.currencyconverter.domain.usecase

import com.wiseman.currencyconverter.domain.model.ExchangeRates
import javax.inject.Inject

class PerformCurrencyExchangeUseCase @Inject constructor() {
    private var counter = 0
    fun calculateCurrencyExchange(
        exchange: ExchangeRates,
        receiptCurrency: String,
        amount: Double,
        commission:Double
    ): Double {

        // 1. Fetch exchange rate from API
        val exchangeRate = exchange.exchangeRates[receiptCurrency]

        // 2. Calculate converted amount
        var convertedAmount = amount * exchangeRate!!

        // 3. Check if commission should be applied
        val commissionFee = if (counter > 7) amount * 0.007 else 0.0

        counter++

        return convertedAmount

        // 5. Update balance
//        updateBalance(fromCurrency, -amount)  // Decrease amount in the source currency
//        updateBalance(toCurrency, convertedAmount)  // Add amount in the target currency
//
//        // 6. Show result
//        showExchangeResult(amount, convertedAmount, commissionFee)
    }

}