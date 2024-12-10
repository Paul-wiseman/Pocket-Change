package com.wiseman.currencyconverter.data.source.local.db.database

import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wiseman.currencyconverter.data.source.local.db.entity.CurrencyEntity
import com.wiseman.currencyconverter.util.coroutine.DispatchProvider
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PrepopulateDatabaseCallback(
    private val dispatchProviders: DispatchProvider
) : RoomDatabase.Callback() {
    private val TAG = "PrepopulateDatabaseCallback"
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val defaultAccounts = listOf(
            CurrencyEntity(100, "EUR", 1000.00),
            CurrencyEntity(1001, "USD", 0.00),
            CurrencyEntity(1002, "GBP", 0.00)
        )
        val exceptionHandler = CoroutineExceptionHandler { context, exception ->
            Log.d(
                TAG,
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
}