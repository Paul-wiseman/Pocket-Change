package com.wiseman.currencyconverter.data.repository

import arrow.core.Either
import com.wiseman.currencyconverter.data.mapper.toAccountType
import com.wiseman.currencyconverter.data.mapper.toAccountTypeEntity
import com.wiseman.currencyconverter.data.mapper.toCurrencyExchangeRates
import com.wiseman.currencyconverter.data.source.local.db.database.CurrenciesDataBase
import com.wiseman.currencyconverter.data.source.local.db.entity.AccountTypeEntity
import com.wiseman.currencyconverter.data.source.local.preference.CurrencyExchangePreference
import com.wiseman.currencyconverter.data.source.remote.RatesService
import com.wiseman.currencyconverter.domain.model.AccountType
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.domain.repository.RatesConversionRepository
import com.wiseman.currencyconverter.util.coroutine.DispatchProvider
import com.wiseman.currencyconverter.util.exception.CurrencyConverterExceptions
import com.wiseman.currencyconverter.util.exception.ErrorMessages.NETWORK_ERROR
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import javax.inject.Inject

class RatesConversionRepositoryImpl @Inject constructor(
    private val service: RatesService,
    private val dispatchProvider: DispatchProvider,
    private val dataBase: CurrenciesDataBase,
    private val preference: CurrencyExchangePreference
) : RatesConversionRepository {
    private val DELAY = 5000L

    override suspend fun getRates(): Flow<Either<CurrencyConverterExceptions, ExchangeRates>> =
        flow {
            while (currentCoroutineContext().isActive) {
                emit(service.getCurrentExchangeRates())
                kotlinx.coroutines.delay(DELAY)
            }
        }
            .map { apiResponse ->
                if (apiResponse.isSuccessful && apiResponse.body() != null) {
                    Either.Right(apiResponse.body()!!.toCurrencyExchangeRates())
                } else {
                    Either.Left(CurrencyConverterExceptions.ApiError(apiResponse.message()))
                }
            }
            .catch { e ->
                Either.Left(CurrencyConverterExceptions.NetworkError(e.message ?: NETWORK_ERROR))
            }
            .flowOn(dispatchProvider.io())

    override fun getAllAccountType(): Flow<List<AccountType>> =
        dataBase.dao.getAllAvailableCurrencies()
            .map { entities -> entities.map { it.toAccountType() } }
            .flowOn(dispatchProvider.io())

    override suspend fun createAccountType(accountType: AccountType) {
        dataBase.dao.insertCurrency(
            AccountTypeEntity(
                currency = accountType.currency,
                value = accountType.value

            )
        )
    }

    override suspend fun updateAccountType(accountType: AccountType) {
        dataBase.dao.updateEntity(accountType.toAccountTypeEntity())
    }

    override fun getTransactionCounter(): Int = preference.getTransactionCounter()


    override fun storeTransactionCount(count: Int) {
        preference.storeTransactionCount(count)
    }
}