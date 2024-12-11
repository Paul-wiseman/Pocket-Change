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
import com.wiseman.currencyconverter.domain.usecase.ExchangeRateValidator
import com.wiseman.currencyconverter.presentation.state.CurrencyExchangeData
import com.wiseman.currencyconverter.presentation.state.RatesViewState
import com.wiseman.currencyconverter.presentation.state.UiEvent
import com.wiseman.currencyconverter.presentation.state.UiState
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
    private val preference: CurrencyExchangePreference,
    private val exchangeRateValidator: ExchangeRateValidator
) : ViewModel() {

    private val _currentExchangeRateState = MutableStateFlow(RatesViewState<ExchangeRates>())
    val currentExchangeRateState: StateFlow<RatesViewState<ExchangeRates>> =
        _currentExchangeRateState
    val currencyTypes = currencyTypesRepository.getAllCurrencyTypes().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        listOf()
    )
    private val _currentExchangeData = MutableStateFlow(CurrencyExchangeData())
    val currentExchangeData: StateFlow<CurrencyExchangeData> get() = _currentExchangeData


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

    fun onEvent(uiEvent: UiEvent) {
        var exchangeData = _currentExchangeData.value
        when (uiEvent) {
            is UiEvent.CalculateCommission -> {
                val commission = commissionCalculator.calculateCommission(uiEvent.totalAmount)
                    .roundToTwoDecimalPlaces()
                exchangeData = exchangeData.copy(commission = commission)
            }

            is UiEvent.CalculateTotalValue -> {
                val totalDeductible =
                    uiEvent.sellingCurrencyAmount +
                            commissionCalculator.calculateCommission(uiEvent.sellingCurrencyAmount)
                exchangeData = exchangeData.copy(totalAmount = totalDeductible)
            }

            is UiEvent.ChangeBuyingCurrency -> {
                exchangeData =
                    exchangeData.copy(buyingCurrency = exchangeData.buyingCurrency.copy(code = uiEvent.currencyCode))
            }

            is UiEvent.ChangeSellingCurrency -> {
                exchangeData =
                    exchangeData.copy(sellingCurrency = exchangeData.sellingCurrency.copy(code = uiEvent.currencyCode))
            }

            is UiEvent.UpdateAmountToBuy -> {
                _currentExchangeRateState.value.data?.let { rates ->
                    val total = convertCurrency(
                        uiEvent.sellingCurrencyAmount,
                        uiEvent.sellingCurrencyCode,
                        uiEvent.buyingCurrencyCode,
                        rates
                    )
                    exchangeData =
                        exchangeData.copy(buyingCurrency = exchangeData.buyingCurrency.copy(value = total))
                }
            }

            is UiEvent.PerformExchange -> handlePerformExchange(uiEvent)
        }
        _currentExchangeData.update { exchangeData }
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
                currentExchangeData.value.sellingCurrency.code,
                uiEvent.sellingCurrencyAmount
            )
            incrementTransactionCounter()
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
    ): ValidationResult = exchangeRateValidator.invoke(
        sellingCurrencyCode = sellingCurrencyCode,
        buyingCurrencyCode = buyingCurrencyCode,
        amount = amount,
        exchangeRates = _currentExchangeRateState.value.data,
        availableCurrency = currencyTypes.value
    )

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


    fun searchExchangeRate(searchText: String): List<Pair<String, Double>> {
        return _currentExchangeRateState.value.data?.currencyRates?.filter { entry: Map.Entry<String, Double?> ->
            entry.key.contains(searchText, true) && entry.value != null
        }?.map { it.key to it.value!! } ?: emptyList()
    }
}
