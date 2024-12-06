package com.wiseman.currencyconverter.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.wiseman.currencyconverter.domain.model.CurrencyRates
import com.wiseman.currencyconverter.domain.repository.RatesConversionRepository
import com.wiseman.currencyconverter.presentation.RatesViewState
import com.wiseman.currencyconverter.presentation.UiState
import com.wiseman.currencyconverter.util.exception.CurrencyConverterExceptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RatesConversionViewModel(
    private val repository: RatesConversionRepository
) : ViewModel() {

    private val _currentExchangeRateState = MutableStateFlow(RatesViewState<CurrencyRates>())
    val currentExchangeRateState: StateFlow<RatesViewState<CurrencyRates>> =
        _currentExchangeRateState

    init {
        getExchangeRate()
    }

    private fun getExchangeRate() {
        viewModelScope.launch {
            repository.getRates()
                .collectLatest { data: Either<CurrencyConverterExceptions, CurrencyRates> ->
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
}