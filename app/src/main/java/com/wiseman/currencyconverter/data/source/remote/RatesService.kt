package com.wiseman.currencyconverter.data.source.remote

import com.wiseman.currencyconverter.data.model.CurrencyExchangeRatesDto
import retrofit2.Response
import retrofit2.http.GET

interface RatesService {
    @GET(Endpoint.RATES_ENDPOINT)
    suspend fun getCurrentExchangeRates(): Response<CurrencyExchangeRatesDto>
}
