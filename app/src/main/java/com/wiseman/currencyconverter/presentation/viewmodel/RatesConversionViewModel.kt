package com.wiseman.currencyconverter.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.wiseman.currencyconverter.data.source.local.preference.CurrencyExchangePreference
import com.wiseman.currencyconverter.domain.model.CurrencyType
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.domain.repository.CurrencyTypesRepository
import com.wiseman.currencyconverter.domain.repository.RatesConversionRepository
import com.wiseman.currencyconverter.domain.usecase.CommissionCalculator
import com.wiseman.currencyconverter.presentation.RatesViewState
import com.wiseman.currencyconverter.presentation.UiEvent
import com.wiseman.currencyconverter.presentation.UiState
import com.wiseman.currencyconverter.util.ValidationResult
import com.wiseman.currencyconverter.util.exception.CurrencyConverterExceptions
import com.wiseman.currencyconverter.util.roundToTwoDecimalPlaces
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
    private val ratesConversionRepository: RatesConversionRepository,
    private val currencyTypesRepository: CurrencyTypesRepository,
    private val commissionCalculator: CommissionCalculator,
    private val preference: CurrencyExchangePreference
) : ViewModel() {

    private val _currentExchangeRateState = MutableStateFlow(RatesViewState<ExchangeRates>())
    val currentExchangeRateState: StateFlow<RatesViewState<ExchangeRates>> =
        _currentExchangeRateState
    val currencyTypes = currencyTypesRepository.getAllCurrencyTypes().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        listOf()
    )
    var selectedCurrencyData = MutableStateFlow(CurrencyExchangeData())


    init {
        getExchangeRate()
    }

    private fun getExchangeRate() {
        viewModelScope.launch {
            ratesConversionRepository.getRates()
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

    fun calculateCommission(amount: Double): Double =
        commissionCalculator.calculateCommission(amount).roundToTwoDecimalPlaces()

    fun updateUiOnEventChange(uiEvent: UiEvent) {
        when (uiEvent) {
            is UiEvent.CalculateCommission -> handleCalculateCommission(uiEvent.totalValue)

            is UiEvent.CalculateTotalValue -> handleCalculateTotalValue(uiEvent.amount)

            is UiEvent.ChangeBuyingCurrency -> handleChangeBuyingCurrency(uiEvent)

            is UiEvent.ChangeSellingCurrency -> handleChangeSellingCurrency(uiEvent)

            is UiEvent.UpdateAmountToBuy -> handleUpdateAmountToBuy(uiEvent)

            is UiEvent.PerformExchange -> handlePerformExchange(uiEvent)
        }
    }

    private fun handlePerformExchange(uiEvent: UiEvent.PerformExchange) {
        _currentExchangeRateState.value.data?.let {
            val amountToBuy = convertCurrency(
                uiEvent.sellingCurrencyAmount,
                uiEvent.sellingCurrencyCode,
                uiEvent.buyingCurrencyCode,
                exchangeRates = it
            )

            createOrUpdateCurrency(
                uiEvent.buyingCurrencyCode,
                amountToBuy
            )
            deductFromCurrency(
                selectedCurrencyData.value.sellingCurrencyCode,
                uiEvent.sellingCurrencyAmount
            )
            incrementTransactionCounter()
        }
    }

    private fun handleUpdateAmountToBuy(uiEvent: UiEvent.UpdateAmountToBuy) {
        _currentExchangeRateState.value.data?.let {
            val total = convertCurrency(
                uiEvent.sellingCurrencyAmount,
                uiEvent.sellingCurrencyCode,
                uiEvent.buyingCurrencyCode,
                it
            )
            selectedCurrencyData.update {
                it.copy(
                    amountToBuy = total
                )
            }
        }
    }

    private fun handleChangeSellingCurrency(uiEvent: UiEvent.ChangeSellingCurrency) {
        selectedCurrencyData.update {
            it.copy(
                sellingCurrencyCode = uiEvent.currencyCode
            )
        }
    }

    private fun handleChangeBuyingCurrency(uiEvent: UiEvent.ChangeBuyingCurrency) {
        selectedCurrencyData.update {
            it.copy(
                buyingCurrencyCode = uiEvent.currencyCode,
            )
        }
    }

    private fun deductFromCurrency(currencyCode: String, amount: Double) {
        viewModelScope.launch {
            val accountType = currencyTypes.value.find { it.currency == currencyCode }
            accountType?.let { account ->
                val totalAmount = account.value - amount
                val newAccountDetail = account.copy(value = totalAmount)
                currencyTypesRepository.updateCurrencyType(newAccountDetail)
            }
        }
    }

    fun performValidation(
        sellingCurrencyCode: String,
        amount: Double,
        buyingCurrencyCode: String
    ): ValidationResult {
        val account = currencyTypes.value.find { it.currency == sellingCurrencyCode }
        return when {
            account == null -> ValidationResult.Error("Please add the currency '$sellingCurrencyCode' to your account before proceeding.")
            amount == 0.00 -> ValidationResult.Error("The amount entered is invalid")
            account.value < (amount + calculateCommission(amount)) -> ValidationResult.Error("Insufficient Balance")
            sellingCurrencyCode == buyingCurrencyCode -> ValidationResult.Error("Transaction on same currency is not allowed")
            _currentExchangeRateState.value.data == null -> ValidationResult.Error("Exchange rates not available")
            else -> ValidationResult.Success
        }
    }

    private fun createOrUpdateCurrency(currencyCode: String, amount: Double) {
        viewModelScope.launch {
            val allAccount = currencyTypes.value
            val accountType = allAccount.find { it.currency == currencyCode }
            accountType?.let { account: CurrencyType ->
                val total = account.value + amount
                val newAccountType = account.copy(value = total)
                currencyTypesRepository.updateCurrencyType(newAccountType)
            } ?: kotlin.run {
                currencyTypesRepository.createCurrencyType(
                    CurrencyType(
                        currency = currencyCode,
                        value = amount
                    )
                )
            }
        }

    }

    private fun handleCalculateCommission(totalValue: Double) {
        selectedCurrencyData.update { it.copy(commission = calculateCommission(totalValue)) }
    }

    private fun handleCalculateTotalValue(amount: Double) {
        val totalDeductible = amount + calculateCommission(amount)
        selectedCurrencyData.update { it.copy(totalAmount = totalDeductible) }
    }


    private fun incrementTransactionCounter() {
        val counter = preference.getTransactionCount() + 1
        preference.setTransactionCount(counter)
    }

    private fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        exchangeRates: ExchangeRates
    ): Double {
        val rateFrom = exchangeRates.currencyRates[fromCurrency] ?: 0.0
        val rateTo = exchangeRates.currencyRates[toCurrency] ?: 0.0

        val conversionFactor = rateTo / rateFrom
        return amount * conversionFactor
    }
}

data class CurrencyExchangeData(
    val sellingCurrencyCode: String = "EUR",
    val buyingCurrencyCode: String = "USD",
    val amountToBuy: Double = 0.00,
    val amountToSell: Double = 0.00,
    val commission: Double = 0.00,
    val totalAmount: Double = 0.00,
)