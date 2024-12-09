package com.wiseman.currencyconverter.data.repository

import android.util.Log
import arrow.core.Either
import com.wiseman.currencyconverter.data.mapper.toAccountType
import com.wiseman.currencyconverter.data.mapper.toAccountTypeEntity
import com.wiseman.currencyconverter.data.mapper.toCurrencyExchangeRates
import com.wiseman.currencyconverter.data.source.local.db.database.CurrenciesDataBase
import com.wiseman.currencyconverter.data.source.local.db.entity.AccountTypeEntity
import com.wiseman.currencyconverter.data.source.local.preference.CurrencyExchangePreference
import com.wiseman.currencyconverter.data.source.remote.RateRatesService
import com.wiseman.currencyconverter.domain.model.AccountType
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.domain.repository.RatesConversionRepository
import com.wiseman.currencyconverter.util.coroutine.DispatchProvider
import com.wiseman.currencyconverter.util.exception.CurrencyConverterExceptions
import com.wiseman.currencyconverter.util.exception.ErrorMessages.NETWORK_ERROR
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import javax.inject.Inject

class RatesConversionRepositoryImpl @Inject constructor(
    private val service: RateRatesService,
    private val dispatchProvider: DispatchProvider,
    private val dataBase: CurrenciesDataBase,
    private val preference: CurrencyExchangePreference
) : RatesConversionRepository {

    override suspend fun getRates(): Flow<Either<CurrencyConverterExceptions, ExchangeRates>> =
        flow {
            while (currentCoroutineContext().isActive) {
                try {
                    val apiResponse = service.getCurrentExchangeRates()
                    if (apiResponse.isSuccessful && apiResponse.body() != null) {
                        emit(Either.Right(apiResponse.body()!!.toCurrencyExchangeRates()))
                    } else {
                        emit(
                            Either.Left(
                                CurrencyConverterExceptions.ApiError(
                                    apiResponse.message().toString()
                                )
                            )
                        )
                    }
                } catch (e: Exception) {
                    emit(
                        Either.Left(
                            CurrencyConverterExceptions.NetworkError(
                                e.message ?: NETWORK_ERROR
                            )
                        )
                    )
                }
                kotlinx.coroutines.delay(DELAY)
            }
        }
            .flowOn(dispatchProvider.io())

    override fun getAllAccountType(): Flow<List<AccountType>> =
        dataBase.dao.getAllAvailableCurrencies()
            .map {
                it.map { accountEntity ->
                    accountEntity.toAccountType()
                }
            }
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

    private companion object {
        const val DELAY = 5000L
    }
}