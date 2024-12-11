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
        availableCurrency:List<CurrencyType>
    ): ValidationResult {
        val sellingCurrency = availableCurrency.find { it.currency == sellingCurrencyCode }
        return when {
            sellingCurrency == null ->
                ValidationResult.Error("Please add the currency '$sellingCurrencyCode' to your account before proceeding.")
            amount == ZERO ->
                ValidationResult.Error("The amount entered is invalid")
            sellingCurrency.value < (amount + commissionCalculator.calculateCommission(amount)) -> ValidationResult.Error(
                "Insufficient Balance"
            )
            sellingCurrencyCode == buyingCurrencyCode ->
                ValidationResult.Error("Transaction on same currency is not allowed")
            exchangeRates == null -> ValidationResult.Error("Exchange rates not available")
            else -> ValidationResult.Success
        }
    }
}
