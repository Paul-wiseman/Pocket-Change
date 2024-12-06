package com.wiseman.currencyconverter.data.source.remote

import com.wiseman.currencyconverter.BuildConfig


object Endpoint {
    private const val BASE_URL = BuildConfig.BASE_URL
    private const val RATES_PATH = "tasks/api/currency-exchange-rates"

    const val RATES_ENDPOINT = "$BASE_URL$RATES_PATH"
}