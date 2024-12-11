package com.wiseman.currencyconverter.presentation.viewmodel

import app.cash.turbine.test
import arrow.core.Either
import com.wiseman.currencyconverter.TestUtils
import com.wiseman.currencyconverter.data.mapper.toCurrencyType
import com.wiseman.currencyconverter.data.source.local.preference.CurrencyExchangePreference
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.domain.repository.CurrencyTypesRepository
import com.wiseman.currencyconverter.domain.repository.RatesConversionRepository
import com.wiseman.currencyconverter.domain.usecase.CommissionCalculator
import com.wiseman.currencyconverter.domain.usecase.ExchangeRateValidator
import com.wiseman.currencyconverter.presentation.state.RatesViewState
import com.wiseman.currencyconverter.presentation.state.UiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
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
    private val scope = TestScope(TestUtils.dispatcher)

    private lateinit var viewModel: RatesConversionViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(TestUtils.dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `getExchangeRate updates currentExchangeRateState with Success when repository emits Right`(): Unit =
        runBlocking {
            val exchangeRates = ExchangeRates(baseCurrency = "EUR", mapOf("USD" to 1.2))
            coEvery { ratesConversionRepository.getRates() } returns flowOf(
                Either.Right(
                    exchangeRates
                )
            )

            val availableCurrencyTypes = TestDataFactory.testCurrencyEntitiesList.map { it.toCurrencyType() }
            every { currencyTypesRepository.getAllCurrencyTypes() } returns flowOf(availableCurrencyTypes)

            viewModel = RatesConversionViewModel(
                ratesConversionRepository,
                currencyTypesRepository,
                commissionCalculator,
                preference,
                exchangeRateValidator
            )

            scope.launch {
                viewModel.currentExchangeRateState.test {
                    assertEquals(
                        RatesViewState<ExchangeRates>(uiState = UiState.Loading),
                        awaitItem()
                    ) // Initial state
                    assertEquals(
                        RatesViewState(uiState = UiState.Success, data = exchangeRates),
                        awaitItem()
                    ) // Success state
                    cancelAndIgnoreRemainingEvents()
                }

            }
        }

}