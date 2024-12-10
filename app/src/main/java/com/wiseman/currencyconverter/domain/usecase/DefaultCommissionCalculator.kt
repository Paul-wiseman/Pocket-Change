package com.wiseman.currencyconverter.domain.usecase

import com.wiseman.currencyconverter.data.source.local.preference.CurrencyExchangePreference
import javax.inject.Inject

class DefaultCommissionCalculator @Inject constructor(
    private val currencyExchangePreference: CurrencyExchangePreference
) : CommissionCalculator {
    override fun calculateCommission(amount: Double): Double {
        return if (currencyExchangePreference.getTransactionCount() > 7) {
            0.007 * amount
        } else 0.00
    }
}