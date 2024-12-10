package com.wiseman.currencyconverter.data.mapper

import com.wiseman.currencyconverter.data.model.CurrencyExchangeRatesDto
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.util.roundToTwoDecimalPlaces

fun CurrencyExchangeRatesDto.toCurrencyExchangeRates() = ExchangeRates(
    baseCurrency = base,
    currencyRates = rates.let { rates ->
        rates::class.java.declaredFields.associate { field ->
            field.isAccessible = true
            val currencyCode = field.name.uppercase()
            val exchangeRates = (field.get(rates) as? Number)?.toDouble()
            currencyCode to exchangeRates?.roundToTwoDecimalPlaces()
        }
            .filterValues { it != null }

    })

