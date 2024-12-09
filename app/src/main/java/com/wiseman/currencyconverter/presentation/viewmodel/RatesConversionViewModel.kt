package com.wiseman.currencyconverter.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.wiseman.currencyconverter.domain.model.AccountType
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.domain.repository.RatesConversionRepository
import com.wiseman.currencyconverter.presentation.RatesViewState
import com.wiseman.currencyconverter.presentation.UiState
import com.wiseman.currencyconverter.util.exception.CurrencyConverterExceptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RatesConversionViewModel @Inject constructor(
    private val repository: RatesConversionRepository,
) : ViewModel() {

    private val _currentExchangeRateState = MutableStateFlow(RatesViewState<ExchangeRates>())
    val currentExchangeRateState: SharedFlow<RatesViewState<ExchangeRates>> =
        _currentExchangeRateState
    val allAccountType = repository.getAllAccountType().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        listOf()
    )
    var selectedCurrencyDataHolder = MutableStateFlow(CurrencyExchangeData())


    init {
        getExchangeRate()
    }

    private fun getExchangeRate() {
        viewModelScope.launch {
            repository.getRates()
                .collectLatest { data: Either<CurrencyConverterExceptions, ExchangeRates> ->
                    when (data) {
                        is Either.Left -> {
                            _currentExchangeRateState.update { ratesViewState ->
                                ratesViewState.copy(
                                    uiState = UiState.Error,
                                    error = data.value.message
                                )
                            }
                        }

                        is Either.Right -> {
                            selectedCurrencyDataHolder.update {
                                it.copy(
                                    receivingCurrencyValue = data.value.exchangeRates[it.receivingCurrency]
                                        ?: 0.00
                                )
                            }

                            _currentExchangeRateState.update { ratesViewState ->
                                ratesViewState.copy(
                                    data = data.value,
                                    uiState = UiState.Success
                                )
                            }
                        }
                    }
                }
        }
    }


    fun changeBuyingCurrency(selectedCurrency: Pair<String, Double>) {
        selectedCurrencyDataHolder.update {
            it.copy(
                receivingCurrency = selectedCurrency.first,
                receivingCurrencyValue = selectedCurrency.second
            )
        }
    }

    fun changeSellingCurrency(selectedCurrency: String) {
        selectedCurrencyDataHolder.update {
            it.copy(
                baseCurrency = selectedCurrency,
            )
        }
    }

    fun calculateExchangeRate(amount: Double, selectedCurrency: String): Double =
        _currentExchangeRateState.value.data?.let {
            calculateCurrencyExchange(
                amount = amount,
                exchange = it,
                receiptCurrency = selectedCurrency
            )
        } ?: 0.00

    private fun calculateCurrencyExchange(
        exchange: ExchangeRates,
        receiptCurrency: String,
        amount: Double,
    ): Double {

        // 1. Fetch exchange rate from API
        val exchangeRate = exchange.exchangeRates[receiptCurrency]

        // 2. Calculate converted amount
        val convertedAmount = amount * exchangeRate!!

        return convertedAmount
    }

    // improve the commission calculation so that it can be extendable
    fun calculateCommission(amount: Double): Double {
        return if (repository.getTransactionCounter() > 7) {
            0.007 * amount
        } else 0.00
    }

    fun createOrUpdateCurrency(currencyCode: String, amount: Double) {
        viewModelScope.launch {
            repository.getAllAccountType().collectLatest { it ->
                val accountType = it.find { it.currency == currencyCode }
                accountType?.let { account: AccountType ->
                    repository.updateAccountType(account.copy(value = amount))
                } ?: kotlin.run {
                    repository.createAccountType(
                        AccountType(
                            currency = currencyCode,
                            value = amount
                        )
                    )
                }
            }
        }
    }

    fun incrementTransactionCounter() {
        val counter = repository.getTransactionCounter() + 1
        repository.storeTransactionCount(counter)
    }

}

data class CurrencyExchangeData(
    val baseCurrency: String = "EUR",
    val receivingCurrency: String = "USD",
    val receivingCurrencyValue: Double = 0.00,
    val baseCurrencyValue: Double = 0.00
)