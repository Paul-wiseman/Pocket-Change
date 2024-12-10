package com.wiseman.currencyconverter.data.repository

import com.wiseman.currencyconverter.data.mapper.toCurrencyEntity
import com.wiseman.currencyconverter.data.mapper.toCurrencyType
import com.wiseman.currencyconverter.data.source.local.db.database.AccountTypeDataBase
import com.wiseman.currencyconverter.data.source.local.db.entity.CurrencyEntity
import com.wiseman.currencyconverter.domain.model.CurrencyType
import com.wiseman.currencyconverter.domain.repository.CurrencyTypesRepository
import com.wiseman.currencyconverter.util.coroutine.DispatchProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CurrencyTypesRepositoryImpl @Inject constructor(
    private val dataBase: AccountTypeDataBase,
    private val dispatchProvider: DispatchProvider,
) : CurrencyTypesRepository {
    override fun getAllCurrencyTypes(): Flow<List<CurrencyType>> =
        dataBase.dao.getAllAvailableCurrencies()
            .map { entities -> entities.map { it.toCurrencyType() } }
            .flowOn(dispatchProvider.io())

    override suspend fun createCurrencyType(currencyType: CurrencyType) {
        dataBase.dao.insert(
            CurrencyEntity(
                currency = currencyType.currency,
                value = currencyType.value

            )
        )
    }

    override suspend fun updateCurrencyType(currencyType: CurrencyType) {
        dataBase.dao.update(currencyType.toCurrencyEntity())
    }
}