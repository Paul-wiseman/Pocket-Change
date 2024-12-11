package com.wiseman.currencyconverter.data.mapper

import com.wiseman.currencyconverter.data.model.CurrencyExchangeRatesDto
import com.wiseman.currencyconverter.domain.model.ExchangeRates
import com.wiseman.currencyconverter.util.roundToTwoDecimalPlaces

/**
 * Converts a [CurrencyExchangeRatesDto] object to an [ExchangeRates] object.
 *
 * This function iterates through the fields of the 'rates' object in the DTO,
 * extracts the currency code and exchange rate for each field, and creates a map
 * where the currency code is the key and the exchange rate is the value.
 * It then filters out any entries with null exchange rates.
 *
 * @receiver The [CurrencyExchangeRatesDto] object to convert.
 * @return An [ExchangeRates] object representing the exchange rates.
 */
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

