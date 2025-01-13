package com.wiseman.currencyconverter.data.repository

import app.cash.turbine.test
import com.wiseman.currencyconverter.data.model.CurrencyExchangeRatesDto
import com.wiseman.currencyconverter.data.source.remote.RatesService
import com.wiseman.currencyconverter.util.TestUtils
import com.wiseman.currencyconverter.util.exception.CurrencyConverterExceptions
import com.wiseman.currencyconverter.util.exception.ErrorMessages.API_ERROR
import com.wiseman.currencyconverter.util.exception.ErrorMessages.NETWORK_ERROR
import com.wiseman.currencyconverter.util.network.NetworkUtil
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import source.TestDataFactory
import java.io.IOException

class RatesConversionRepositoryImplTest {
    private val mockRatesService: RatesService = mockk()
    private lateinit var ratesConversionRepositoryImpl: RatesConversionRepositoryImpl
    private val scope = TestScope(TestUtils.dispatcher)
    private val mockNetworkUtil: NetworkUtil = mockk()

    @BeforeEach
    fun setUp() {
        ratesConversionRepositoryImpl = RatesConversionRepositoryImpl(
            mockRatesService,
            TestUtils.testDispatchProvider,
            mockNetworkUtil,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }


    @Test
    fun `getRates emits NetworkError when internet is unavailable`(): Unit = runBlocking {
        every { mockNetworkUtil.isConnected } returns flowOf(false)

        scope.launch {
            ratesConversionRepositoryImpl.getRates().test {
                val emission = awaitItem()
                assertTrue(emission.isLeft())
                assertEquals(
                    CurrencyConverterExceptions.NetworkError(NETWORK_ERROR),
                    emission.swap().getOrNull()
                )
                awaitComplete()
            }
        }
    }

    @Test
    fun `getRates emits ExchangeRates when internet is available and request is successful`() =
        runTest {
            val exchangeRates = TestDataFactory.getTestExchangeRates()

            val response: Response<CurrencyExchangeRatesDto> = mockk {
                every { isSuccessful } returns true
                every { body() } returns exchangeRates
            }

            every { mockNetworkUtil.isConnected } returns flowOf(true)
            coEvery { mockRatesService.getCurrentExchangeRates() } returns response

            scope.launch {
                ratesConversionRepositoryImpl.getRates().test {
                    val emission = awaitItem()
                    assertTrue(emission.isRight())
                    assertEquals(exchangeRates, emission.getOrNull())
                    awaitComplete()
                }
            }
        }

    @Test
    fun `getRates emits ApiError when internet is available but request is unsuccessful`() =
        runTest {
            val response: Response<CurrencyExchangeRatesDto> = mockk {
                every { isSuccessful } returns false
                every { body() } returns null
            }

            every { mockNetworkUtil.isConnected } returns flowOf(true)
            coEvery { mockRatesService.getCurrentExchangeRates() } returns response

            scope.launch {
                ratesConversionRepositoryImpl.getRates().test {
                    val emission = awaitItem()
                    assertTrue(emission.isLeft())
                    assertEquals(
                        CurrencyConverterExceptions.ApiError(API_ERROR),
                        emission.swap().getOrNull()
                    )
                    awaitComplete()
                }
            }

        }

    @Test
    fun `getRates emits NetworkError when internet is available but request throws exception`() =
        runTest {
            val exception = IOException("Network error")
            every { mockNetworkUtil.isConnected } returns flowOf(true)
            coEvery { mockRatesService.getCurrentExchangeRates() } throws exception

            scope.launch {
                ratesConversionRepositoryImpl.getRates().test {
                    val emission = awaitItem()
                    assertTrue(emission.isLeft())
                    assertEquals(
                        CurrencyConverterExceptions.NetworkError(exception.message!!),
                        emission.swap().getOrNull()
                    )
                    awaitComplete()
                }
            }
        }

}