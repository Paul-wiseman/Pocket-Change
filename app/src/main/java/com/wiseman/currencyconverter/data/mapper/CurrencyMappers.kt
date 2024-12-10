package com.wiseman.currencyconverter.data.mapper

import com.wiseman.currencyconverter.data.source.local.db.entity.CurrencyEntity
import com.wiseman.currencyconverter.domain.model.CurrencyType
import com.wiseman.currencyconverter.util.roundToTwoDecimalPlaces


/**
 * Converts a [CurrencyEntity] to a [CurrencyType].
 *
 * This function maps the properties of a CurrencyEntity object to a CurrencyType object.
 * It also rounds the 'value' property to two decimal places.
 *
 * @return A [CurrencyType] object representing the converted [CurrencyEntity].
 */
fun CurrencyEntity.toCurrencyType() = CurrencyType(
    id = id,
    currency = currency,
    value = value.roundToTwoDecimalPlaces()
)


/**
 * Converts a [CurrencyType] to a [CurrencyEntity].
 *
 * This function maps the properties of a CurrencyType object to a CurrencyEntity object.
 * It also rounds the 'value' property to two decimal places.
 *
 * @return A [CurrencyEntity] object representing the converted [CurrencyType].
 */
fun CurrencyType.toCurrencyEntity() = CurrencyEntity(
    id = id,
    currency = currency,
    value = value.roundToTwoDecimalPlaces()
)