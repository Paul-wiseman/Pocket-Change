package com.wiseman.currencyconverter.domain.repository

import com.wiseman.currencyconverter.domain.model.CurrencyType
import kotlinx.coroutines.flow.Flow

interface CurrencyTypesRepository {
    fun getAllCurrencyTypes(): Flow<List<CurrencyType>> // Renamed to getAllCurrencyTypes
    suspend fun createCurrencyType(currencyType: CurrencyType)
    suspend fun updateCurrencyType(currencyType: CurrencyType)
}