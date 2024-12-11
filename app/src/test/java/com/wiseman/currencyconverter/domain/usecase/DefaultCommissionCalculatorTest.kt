package com.wiseman.currencyconverter.domain.usecase

import com.wiseman.currencyconverter.data.source.local.preference.CurrencyExchangePreference
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultCommissionCalculatorTest {

    private val currencyExchangePreference: CurrencyExchangePreference = mockk()
    private lateinit var commissionCalculator: DefaultCommissionCalculator

    @BeforeEach
    fun setup() {
        commissionCalculator = DefaultCommissionCalculator(currencyExchangePreference)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `calculateCommission returns 0 when transaction count is less than or equal to FREE_TRANSACTIONS_LIMIT`() {
        every { currencyExchangePreference.getTransactionCount() } returns 5

        val amount = 100.0
        val commission = commissionCalculator.calculateCommission(amount)

        assertEquals(0.0, commission)
    }

    @Test
    fun `calculateCommission returns commission when transaction count is greater than FREE_TRANSACTIONS_LIMIT`() {
        every { currencyExchangePreference.getTransactionCount() } returns 8

        val amount = 100.0
        val expectedCommission = COMMISSION_RATE * amount
        val commission = commissionCalculator.calculateCommission(amount)

        assertEquals(expectedCommission, commission)
    }

    private companion object {
        const val COMMISSION_RATE = 0.007
    }
}