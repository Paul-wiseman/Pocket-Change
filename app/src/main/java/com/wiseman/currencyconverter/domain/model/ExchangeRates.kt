package com.wiseman.currencyconverter.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExchangeRates(
    val baseCurrency: String,
    val exchangeRates: Map<String, Double?>
) : Parcelable
