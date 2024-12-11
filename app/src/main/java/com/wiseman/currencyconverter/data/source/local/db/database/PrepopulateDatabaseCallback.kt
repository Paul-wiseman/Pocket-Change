package com.wiseman.currencyconverter.data.source.local.db.database

import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wiseman.currencyconverter.data.source.local.db.entity.CurrencyEntity
import com.wiseman.currencyconverter.util.Constants.ZERO
import com.wiseman.currencyconverter.util.coroutine.DispatchProvider
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PrepopulateDatabaseCallback(
    private val dispatchProviders: DispatchProvider
) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val defaultAccounts = listOf(
            CurrencyEntity(EUR_ID, "EUR", INITIAL_EUR_BALANCE),
            CurrencyEntity(USD_ID, "USD", ZERO),
            CurrencyEntity(GBP_ID, "GBP", ZERO)
        )
        val exceptionHandler = CoroutineExceptionHandler { context, exception ->
            Log.d(
                "PrepopulateDatabaseCallback",
                " Failure to create database with default Entities - ${exception.printStackTrace()} "
            )
        }
        val scope = CoroutineScope(dispatchProviders.io() + exceptionHandler)
        scope.launch {
            val accountTypesDatabaseDao = (db as AccountTypeDataBase).dao
            defaultAccounts.forEach {
                accountTypesDatabaseDao.insert(it)
            }
        }
    }

    private companion object {
        const val EUR_ID = 100
        const val USD_ID = 1001
        const val GBP_ID = 1002
        const val INITIAL_EUR_BALANCE = 1000.00
    }

}

