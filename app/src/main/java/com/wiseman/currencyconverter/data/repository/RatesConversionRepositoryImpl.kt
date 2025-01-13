package com.wiseman.currencyconverter.data.repository

import arrow.core.Either
import com.wiseman.currencyconverter.data.mapper.toCurrencyExchangeRates
import com.wiseman.currencyconverter.data.source.remote.RatesService
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.domain.repository.RatesConversionRepository
import com.wiseman.currencyconverter.util.coroutine.DispatchProvider
import com.wiseman.currencyconverter.util.exception.CurrencyConverterExceptions
import com.wiseman.currencyconverter.util.exception.ErrorMessages.API_ERROR
import com.wiseman.currencyconverter.util.exception.ErrorMessages.INVALID_RESPONSE
import com.wiseman.currencyconverter.util.exception.ErrorMessages.NETWORK_ERROR
import com.wiseman.currencyconverter.util.network.NetworkUtil
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import javax.inject.Inject

class RatesConversionRepositoryImpl @Inject constructor(
    private val service: RatesService,
    private val dispatchProvider: DispatchProvider,
    private val networkUtil: NetworkUtil,
) : RatesConversionRepository {

    override suspend fun getRates(): Flow<Either<CurrencyConverterExceptions, ExchangeRates>> =
        channelFlow {
            while (currentCoroutineContext().isActive) {
                networkUtil.isConnected.collectLatest { networkStatus ->
                    if (networkStatus) {
                        send(makeRequest().await())
                        kotlinx.coroutines.delay(REFRESH_TIME_INTERVAL)
                    } else {
                        send(
                            Either.Left(
                                CurrencyConverterExceptions.NetworkError(
                                    NETWORK_ERROR
                                )
                            )
                        )
                    }
                }
            }
        }.flowOn(dispatchProvider.io())


    private suspend fun makeRequest(): Deferred<Either<CurrencyConverterExceptions, ExchangeRates>> =
        coroutineScope {
            async {
                try {
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


    private companion object {
        const val REFRESH_TIME_INTERVAL = 1000L
    }
}
