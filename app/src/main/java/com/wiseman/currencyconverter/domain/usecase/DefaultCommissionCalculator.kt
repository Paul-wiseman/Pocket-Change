package com.wiseman.currencyconverter.domain.usecase

import com.wiseman.currencyconverter.data.source.local.preference.CurrencyExchangePreference
import javax.inject.Inject

class DefaultCommissionCalculator @Inject constructor(
    private val currencyExchangePreference: CurrencyExchangePreference
) : CommissionCalculator {
    override fun calculateCommission(amount: Double): Double {
        return if (currencyExchangePreference.getTransactionCount() > FREE_TRANSACTIONS_LIMIT) {
            COMMISSION_RATE * amount
        } else 0.00
    }

    private companion object{
        const val FREE_TRANSACTIONS_LIMIT = 7
        const val COMMISSION_RATE = 0.007
    }
}
