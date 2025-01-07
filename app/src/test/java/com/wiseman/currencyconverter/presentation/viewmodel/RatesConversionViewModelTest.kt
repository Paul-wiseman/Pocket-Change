package com.wiseman.currencyconverter.presentation.viewmodel

import arrow.core.Either
import com.wiseman.currencyconverter.data.mapper.toCurrencyType
import com.wiseman.currencyconverter.data.source.local.preference.CurrencyExchangePreference
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.domain.repository.CurrencyTypesRepository
import com.wiseman.currencyconverter.domain.repository.RatesConversionRepository
import com.wiseman.currencyconverter.domain.usecase.CommissionCalculator
import com.wiseman.currencyconverter.domain.usecase.ExchangeRateValidator
import com.wiseman.currencyconverter.presentation.state.RatesViewState
import com.wiseman.currencyconverter.presentation.state.UiState
import com.wiseman.currencyconverter.util.exception.CurrencyConverterExceptions
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import source.TestDataFactory

@OptIn(ExperimentalCoroutinesApi::class)
class RatesConversionViewModelTest {
    private val ratesConversionRepository: RatesConversionRepository = mockk()
    private val currencyTypesRepository: CurrencyTypesRepository = mockk()
    private val commissionCalculator: CommissionCalculator = mockk()
    private val preference: CurrencyExchangePreference = mockk()
    private val exchangeRateValidator: ExchangeRateValidator = mockk()
    private val exchangeRates = ExchangeRates(baseCurrency = "EUR", mapOf("USD" to 1.2))
    private val availableCurrencyTypes =
        TestDataFactory.testCurrencyEntitiesList.map { it.toCurrencyType() }
    private lateinit var viewModel: RatesConversionViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { currencyTypesRepository.getAllCurrencyTypes() } returns flowOf(
            availableCurrencyTypes
        )
        viewModel = RatesConversionViewModel(
            ratesConversionRepository,
            currencyTypesRepository,
            commissionCalculator,
            preference,
            exchangeRateValidator
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `getExchangeRate updates currentExchangeRateState with Success when repository emits Right`(): Unit =
        runTest {
            coEvery { ratesConversionRepository.getRates() } returns flowOf(
                Either.Right(
                    exchangeRates
                )
            )

            viewModel.getExchangeRate()

            assertEquals(
                RatesViewState(uiState = UiState.Success, data = exchangeRates),
                viewModel.currentExchangeRateState.value
            )
        }

    @Test
    fun `getExchangeRate updates currentExchangeRateState with error when repository emits Left`() =
        runTest {
            val errorMessage = "SomeThing went wrong"
            val exception = CurrencyConverterExceptions.NetworkError(errorMessage)
            coEvery { ratesConversionRepository.getRates() } returns flowOf(Either.Left(exception))

            viewModel.getExchangeRate()

            assertEquals(
                RatesViewState(uiState = UiState.Error, data = null, error = errorMessage),
                viewModel.currentExchangeRateState.value
            )
        }
}