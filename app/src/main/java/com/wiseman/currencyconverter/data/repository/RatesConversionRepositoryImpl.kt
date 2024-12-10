package com.wiseman.currencyconverter.data.repository

import android.content.Context
import arrow.core.Either
import com.wiseman.currencyconverter.data.mapper.toCurrencyExchangeRates
import com.wiseman.currencyconverter.data.source.remote.RatesService
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.domain.repository.RatesConversionRepository
import com.wiseman.currencyconverter.util.NetworkUtil
import com.wiseman.currencyconverter.util.coroutine.DispatchProvider
import com.wiseman.currencyconverter.util.exception.CurrencyConverterExceptions
import com.wiseman.currencyconverter.util.exception.ErrorMessages.API_ERROR
import com.wiseman.currencyconverter.util.exception.ErrorMessages.INVALID_RESPONSE
import com.wiseman.currencyconverter.util.exception.ErrorMessages.NETWORK_ERROR
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import javax.inject.Inject

class RatesConversionRepositoryImpl @Inject constructor(
    private val service: RatesService,
    private val dispatchProvider: DispatchProvider,
    private val networkUtil: NetworkUtil,
    @ApplicationContext private val context: Context
) : RatesConversionRepository {
    private val refreshInterval = 5000L

    override suspend fun getRates(): Flow<Either<CurrencyConverterExceptions, ExchangeRates>> =
        flow {
            if (networkUtil.isInternetAvailable(context)) {
                while (currentCoroutineContext().isActive) {
                    emit(makeRequest())
                    kotlinx.coroutines.delay(refreshInterval)
                }
            } else {
                emit(
                    Either.Left(
                        CurrencyConverterExceptions.NetworkError(
                            NETWORK_ERROR
                        )
                    )
                )
            }
        }
            .flowOn(dispatchProvider.io())

    private suspend fun makeRequest(): Either<CurrencyConverterExceptions, ExchangeRates> {
        return try {
            val apiResponse = service.getCurrentExchangeRates()
            apiResponse.body()?.let { responseBody ->
                if (apiResponse.isSuccessful) {
                    Either.Right(responseBody.toCurrencyExchangeRates())
                } else {
                    Either.Left(CurrencyConverterExceptions.ApiError(API_ERROR))
                }
            } ?: Either.Left(CurrencyConverterExceptions.ApiError(INVALID_RESPONSE))
        } catch (e: Exception) {
            Either.Left(
                CurrencyConverterExceptions.NetworkError(
                    e.message ?: NETWORK_ERROR
                )
            )
        }
    }
}