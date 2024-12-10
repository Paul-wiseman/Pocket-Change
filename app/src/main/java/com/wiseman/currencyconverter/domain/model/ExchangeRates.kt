package com.wiseman.currencyconverter.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExchangeRates(
    val baseCurrency: String,
    val currencyRates: Map<String, Double?>
) : Parcelable
