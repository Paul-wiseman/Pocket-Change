package com.wiseman.currencyconverter.domain.repository

import arrow.core.Either
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.util.exception.CurrencyConverterExceptions
import kotlinx.coroutines.flow.Flow

interface RatesConversionRepository {
    suspend fun getRates(): Flow<Either<CurrencyConverterExceptions, ExchangeRates>>
}