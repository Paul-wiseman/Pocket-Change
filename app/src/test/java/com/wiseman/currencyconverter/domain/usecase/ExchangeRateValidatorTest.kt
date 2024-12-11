package com.wiseman.currencyconverter.domain.usecase

import com.wiseman.currencyconverter.domain.model.CurrencyType
import com.wiseman.currencyconverter.util.ValidationResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExchangeRateValidatorTest {
    private val commissionCalculator: CommissionCalculator = mockk()
    private lateinit var validator: ExchangeRateValidator


    @BeforeEach
    fun setup() {
        validator = ExchangeRateValidator(commissionCalculator)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke returns Error when selling currency is not found`() {

        val availableCurrencyType = listOf(
            CurrencyType(currency = "USD", value = 0.0)
        )
        every { commissionCalculator.calculateCommission(any()) } returns 0.0

        val result = validator(
            sellingCurrencyCode = "EUR",
            buyingCurrencyCode = "USD",
            amount = 10.0,
            exchangeRates = mockk(),
            availableCurrency = availableCurrencyType
        )

        assertTrue(result is ValidationResult.Error)
        assertEquals(
            "Please add the currency 'EUR' to your account before proceeding.",
            (result as ValidationResult.Error).errorMessage
        )
    }

    @Test
    fun `invoke returns Error when amount is zero`() {
        val availableCurrencyType = listOf(
            CurrencyType(currency = "EUR", value = 100.0),
            CurrencyType(currency = "USD", value = 0.0)
        )
        val result = validator(
            sellingCurrencyCode = "USD",
            buyingCurrencyCode = "EUR",
            amount = 0.0,
            exchangeRates = mockk(),
            availableCurrency = availableCurrencyType
        )

        assertTrue(result is ValidationResult.Error)
        assertEquals(
            "The amount entered is invalid",
            (result as ValidationResult.Error).errorMessage
        )
    }

    @Test
    fun `invoke returns Error when insufficient balance`() {
        val availableCurrencyType = listOf(
            CurrencyType(currency = "EUR", value = 100.0),
            CurrencyType(currency = "USD", value = 0.0)
        )
        every { commissionCalculator.calculateCommission(any()) } returns 1.0
        val result = validator(
            sellingCurrencyCode = "USD",
            buyingCurrencyCode = "EUR",
            amount = 10.0,
            exchangeRates = mockk(),
            availableCurrency = availableCurrencyType
        )

        assertTrue(result is ValidationResult.Error)
        assertEquals("Insufficient Balance", (result as ValidationResult.Error).errorMessage)
    }

    @Test
    fun `invoke returns Error when selling and buying currencies are the same`() {
        val availableCurrencyType = listOf(
            CurrencyType(currency = "EUR", value = 10.0),
            CurrencyType(currency = "USD", value = 100.0)
        )
        every { commissionCalculator.calculateCommission(any()) } returns 0.0

        val result = validator(
            sellingCurrencyCode = "USD",
            buyingCurrencyCode = "USD",
            amount = 10.0,
            exchangeRates = mockk(),
            availableCurrency = availableCurrencyType
        )

        assertTrue(result is ValidationResult.Error)
        assertEquals(
            ExchangeRateValidator.TRANSACTION_ON_SAME_CURRENCY_ERROR,
            (result as ValidationResult.Error).errorMessage
        )
    }

    @Test
    fun `invoke returns Error when exchange rates are not available`() {

        val availableCurrencyType = listOf(
            CurrencyType(currency = "EUR", value = 100.0),
            CurrencyType(currency = "USD", value = 100.0)
        )
        every { commissionCalculator.calculateCommission(any()) } returns 0.0

        val result = validator(
            sellingCurrencyCode = "USD",
            buyingCurrencyCode = "EUR",
            amount = 10.0,
            exchangeRates = null,
            availableCurrency = availableCurrencyType
        )

        assertTrue(result is ValidationResult.Error)
        assertEquals(
            "Exchange rates not available",
            (result as ValidationResult.Error).errorMessage
        )
    }

    @Test
    fun `invoke returns Success when validation passes`() {
        val availableCurrencyType = listOf(
            CurrencyType(currency = "EUR", value = 0.0),
            CurrencyType(currency = "USD", value = 100.0)
        )
        every { commissionCalculator.calculateCommission(any()) } returns 0.0
        val result = validator(
            sellingCurrencyCode = "USD",
            buyingCurrencyCode = "EUR",
            amount = 10.0,
            exchangeRates = mockk(),
            availableCurrency = availableCurrencyType
        )
        assertTrue(result is ValidationResult.Success)
    }
}