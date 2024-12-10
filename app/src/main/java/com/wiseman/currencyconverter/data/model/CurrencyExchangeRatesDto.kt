package com.wiseman.currencyconverter.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrencyExchangeRatesDto(
    @SerialName("base")
    val base: String,
    @SerialName("date")
    val date: String?,
    @SerialName("rates")
    val rates: Rates
)