package com.wiseman.currencyconverter.domain.usecase

import com.wiseman.currencyconverter.domain.model.CurrencyType
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.util.Constants.ZERO
import com.wiseman.currencyconverter.util.ValidationResult
import javax.inject.Inject

class ExchangeRateValidator @Inject constructor(
    private val commissionCalculator: CommissionCalculator,
) {

    operator fun invoke(
        sellingCurrencyCode: String,
        buyingCurrencyCode: String,
        amount: Double,
        exchangeRates: ExchangeRates?,
        availableCurrency: List<CurrencyType>
    ): ValidationResult {
        val sellingCurrency = availableCurrency.find { it.currency == sellingCurrencyCode }
        return when {
            sellingCurrency == null ->
                ValidationResult.Error("Please add the currency '$sellingCurrencyCode' to your account before proceeding.")

            amount == ZERO ->
                ValidationResult.Error(INVALID_AMOUNT_ERROR)

            sellingCurrency.value < (amount + commissionCalculator.calculateCommission(amount)) -> ValidationResult.Error(
                INSUFFICIENT_BALANCE_ERROR
            )

            sellingCurrencyCode == buyingCurrencyCode ->
                ValidationResult.Error(TRANSACTION_ON_SAME_CURRENCY_ERROR)

            exchangeRates == null -> ValidationResult.Error(EXCHANGE_RATE_NOT_AVAILABLE_ERROR)
            else -> ValidationResult.Success
        }
    }


    companion object {
        const val INVALID_AMOUNT_ERROR = "The amount entered is invalid"
        const val TRANSACTION_ON_SAME_CURRENCY_ERROR = "Transaction on same currency is not allowed"
        const val EXCHANGE_RATE_NOT_AVAILABLE_ERROR = "Exchange rates not available"
        const val INSUFFICIENT_BALANCE_ERROR = "Insufficient Balance"
    }
}
