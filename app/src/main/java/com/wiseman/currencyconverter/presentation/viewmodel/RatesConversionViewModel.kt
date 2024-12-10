package com.wiseman.currencyconverter.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.wiseman.currencyconverter.domain.model.AccountType
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.domain.repository.RatesConversionRepository
import com.wiseman.currencyconverter.presentation.RatesViewState
import com.wiseman.currencyconverter.presentation.UiEvent
import com.wiseman.currencyconverter.presentation.UiState
import com.wiseman.currencyconverter.util.exception.CurrencyConverterExceptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    val currentExchangeRateState: StateFlow<RatesViewState<ExchangeRates>> =
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
                            _currentExchangeRateState.update { ratesViewState ->
                                data.value.copy(
                                    exchangeRates = data.value.exchangeRates
                                )
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
    // improve the commission calculation so that it can be extendable
    fun calculateCommission(amount: Double): Double {
        return if (repository.getTransactionCounter() > 7) {
            0.007 * amount
        } else 0.00
    }

    fun updateUiOnEventChange(uiEvent: UiEvent) {
        when (uiEvent) {
            is UiEvent.CalculateCommission -> {
                selectedCurrencyDataHolder.update { it.copy(commission = calculateCommission(uiEvent.totalValue)) }
            }

            is UiEvent.CalculateTotalValue -> {
                val exchangeRate = _currentExchangeRateState.value.data?.exchangeRates?.get(
                    selectedCurrencyDataHolder.value.buyingCurrencyCode
                )


                   val totalDeductible = uiEvent.amount + calculateCommission(uiEvent.amount)
                    selectedCurrencyDataHolder.update { it.copy(totalAmount = totalDeductible) }



            }

            is UiEvent.ChangeBuyingCurrency -> selectedCurrencyDataHolder.update {
                it.copy(
                    buyingCurrencyCode = uiEvent.currencyCode,
                    exchangeRate = uiEvent.exchangeRate
                )
            }

            is UiEvent.ChangeSellingCurrency -> selectedCurrencyDataHolder.update {
                it.copy(
                    sellingCurrencyCode = uiEvent.currencyCode
                )
            }

            is UiEvent.UpdateAmountToBuy -> {
                val exchangeRate = selectedCurrencyDataHolder.value.exchangeRate
                val totalValue = exchangeRate * uiEvent.amountToSell
                selectedCurrencyDataHolder.update {
                    it.copy(
                        amountToBuy = totalValue
                    )
                }
            }

            is UiEvent.PerformExchange -> {
                // perform validation
                createOrUpdateCurrency(
                    uiEvent.buyingCurrencyCode,
                    uiEvent.buyingCurrencyAmount
                )
                deductFromCurrency(
                    selectedCurrencyDataHolder.value.sellingCurrencyCode,
                    uiEvent.sellingCurrencyAmount
                )
                incrementTransactionCounter()
            }

            is UiEvent.UpdateExchangeRate -> selectedCurrencyDataHolder.update {
                it.copy(
                    exchangeRate = _currentExchangeRateState.value.data?.exchangeRates?.get(
                        uiEvent.currency
                    ) ?: 0.00
                )
            }
        }

    }

    private fun deductFromCurrency(currencyCode: String, amount: Double) {
        viewModelScope.launch {
            val accountType = allAccountType.value.find { it.currency == currencyCode }
            accountType?.let {account->
                val totalAmount = account.value - amount
                val newAccountDetail = account.copy(value = totalAmount)
                repository.updateAccountType(newAccountDetail)
            }
        }
    }

    fun performValidation(currencyCode: String, amount: Double): Boolean {
        // check if the account exist and if the balance is above the buyingCurrencyAmount inputed
        return allAccountType.value.find { it.currency == currencyCode }?.let { accuntType ->
            if (accuntType.value < amount) false else true
        } ?: false
    }

    private fun createOrUpdateCurrency(currencyCode: String, amount: Double) {
        viewModelScope.launch {
            val allAccount = allAccountType.value
            val accountType = allAccount.find { it.currency == currencyCode }
            accountType?.let { account: AccountType ->
                val total = account.value + amount
                val newAccountType = account.copy(value = total)
                repository.updateAccountType(newAccountType)
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

    private fun incrementTransactionCounter() {
        val counter = repository.getTransactionCounter() + 1
        repository.storeTransactionCount(counter)
    }

}

data class CurrencyExchangeData(
    val sellingCurrencyCode: String = "EUR",
    val buyingCurrencyCode: String = "USD",
    val amountToBuy: Double = 0.00,
    val amountToSell: Double = 0.00,
    val commission: Double = 0.00,
    val totalAmount: Double = 0.00,
    val exchangeRate: Double = 0.00
)