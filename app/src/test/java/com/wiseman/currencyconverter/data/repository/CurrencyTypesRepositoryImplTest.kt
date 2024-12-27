package com.wiseman.currencyconverter.data.repository

import app.cash.turbine.test
import com.wiseman.currencyconverter.util.TestUtils
import com.wiseman.currencyconverter.data.mapper.toCurrencyType
import com.wiseman.currencyconverter.data.source.local.db.database.AccountTypeDataBase
import com.wiseman.currencyconverter.data.source.local.db.doa.CurrencyDao
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import source.TestDataFactory

class CurrencyTypesRepositoryImplTest {

    private val dataBase: AccountTypeDataBase = mockk()
    private val dao: CurrencyDao = mockk()

    private lateinit var repository: CurrencyTypesRepositoryImpl

    @BeforeEach
    fun setup() {
        every { dataBase.dao } returns dao
        repository = CurrencyTypesRepositoryImpl(dataBase, TestUtils.testDispatchProvider)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getAllCurrencyTypes emits currency types from database`(): Unit = runBlocking {
        val currencyEntities = TestDataFactory.testCurrencyEntitiesList
        val expectedCurrencyTypes = currencyEntities.map { it.toCurrencyType() }

        every { dao.getAllAvailableCurrencies() } returns flowOf(currencyEntities)

        repository.getAllCurrencyTypes().test {
            assertEquals(expectedCurrencyTypes, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `createCurrencyType inserts currency entity into database`(): Unit = runBlocking {
        val currencyEntity = TestDataFactory.testCurrencyEntitiesList[0]
        val currencyType = currencyEntity.toCurrencyType()

        coJustRun { dao.insert(currencyEntity) }

        repository.createCurrencyType(currencyType)

        coVerify { dao.insert(currencyEntity) }
    }

    @Test
    fun `updateCurrencyType updates currency entity in database`(): Unit = runBlocking {
        val currencyEntity = TestDataFactory.testCurrencyEntitiesList[0]
        val currencyType = currencyEntity.toCurrencyType()

        coJustRun { dao.update(currencyEntity) }

        repository.updateCurrencyType(currencyType)

        coVerify { dao.update(currencyEntity) }
    }
}
