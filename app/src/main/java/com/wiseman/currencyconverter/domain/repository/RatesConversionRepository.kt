package com.wiseman.currencyconverter.domain.repository

import arrow.core.Either
import com.wiseman.currencyconverter.data.source.local.db.entity.AccountTypeEntity
import com.wiseman.currencyconverter.domain.model.AccountType
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.util.exception.CurrencyConverterExceptions
import kotlinx.coroutines.flow.Flow

interface RatesConversionRepository {
    suspend fun getRates(): Flow<Either<CurrencyConverterExceptions, ExchangeRates>>
    fun getAllAccountType(): Flow<List<AccountType>>
    suspend fun createAccountType(accountType: AccountType)
    suspend fun updateAccountType(accountType: AccountType)
    fun getTransactionCounter(): Int
    fun storeTransactionCount(count: Int)
}