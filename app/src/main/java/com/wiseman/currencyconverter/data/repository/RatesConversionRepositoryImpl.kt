package com.wiseman.currencyconverter.data.repository

import arrow.core.Either
import com.wiseman.currencyconverter.data.mapper.toCurrencyExchangeRates
import com.wiseman.currencyconverter.data.source.remote.RateRatesService
import com.wiseman.currencyconverter.domain.model.CurrencyRates
import com.wiseman.currencyconverter.domain.repository.RatesConversionRepository
import com.wiseman.currencyconverter.util.coroutine.DispatchProvider
import com.wiseman.currencyconverter.util.exception.CurrencyConverterExceptions
import com.wiseman.currencyconverter.util.exception.ErrorMessages.NETWORK_ERROR
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import javax.inject.Inject

class RatesConversionRepositoryImpl @Inject constructor(
    private val service: RateRatesService,
    private val dispatchProvider: DispatchProvider
) : RatesConversionRepository {

    override suspend fun getRates(): Flow<Either<CurrencyConverterExceptions, CurrencyRates>> =
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

    private companion object {
        const val DELAY = 5000L
    }
}