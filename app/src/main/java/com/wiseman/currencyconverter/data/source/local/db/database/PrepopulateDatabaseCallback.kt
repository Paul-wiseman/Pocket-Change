package com.wiseman.currencyconverter.data.source.local.db.database

import android.content.ContentValues
import android.util.Log
import androidx.room.OnConflictStrategy
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wiseman.currencyconverter.data.source.local.db.entity.CurrencyEntity
import com.wiseman.currencyconverter.util.Constants.CURRENCY_ENTITY_TABLE_NAME
import com.wiseman.currencyconverter.util.coroutine.DispatchProvider
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A Room database callback class that prepopulates the database with initial data
 * when the database is created for the first time.
 *
 * This callback inserts default currency entities (EUR, USD, GBP) into the database.
 * EUR is initialized with a balance of 1000.00, while USD and GBP start with a balance of 0.
 *
 * the initial balance serves a starting balance for transactions on the app.
 *
 *
 * @param dispatchProviders An instance of [DispatchProvider] for managing coroutines.
 */
class PrepopulateDatabaseCallback(
    private val dispatchProviders: DispatchProvider
) : RoomDatabase.Callback() {

    private val exceptionHandler = CoroutineExceptionHandler { context, exception ->
        Log.d(
            "PrepopulateDatabaseCallback",
            " Failure to create database with default Entities - ${exception.printStackTrace()} "
        )
    }
    private val scope = CoroutineScope(dispatchProviders.io() + exceptionHandler)

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        scope.launch {
            try {

                db.beginTransaction()
                insertDefaultAccounts(db)
                db.setTransactionSuccessful()

            } catch (e: Exception) {
                Log.d("PrepopulateDatabaseCallback", "failed to create database - $e ")
            } finally {
                db.endTransaction()
            }
        }
    }

    private fun insertDefaultAccounts(
        db: SupportSQLiteDatabase,
    ) {

        val defaultAccounts = getDefaultAccounts()
        defaultAccounts.forEach { currencyEntity ->
            val values = ContentValues().apply {
                put(ID, currencyEntity.id)
                put(CURRENCY, currencyEntity.currency)
                put(VALUE, currencyEntity.value)

            }
            db.insert(
                TABLE_NAME,
                conflictAlgorithm = OnConflictStrategy.REPLACE,
                values = values,
            )
        }
    }

    private fun getDefaultAccounts(): List<CurrencyEntity> = listOf(
        CurrencyEntity(100, "EUR", 1000.00),
        CurrencyEntity(1001, "USD", 0.00),
        CurrencyEntity(1002, "GBP", 0.00)
    )

    private companion object {
        const val TABLE_NAME = CURRENCY_ENTITY_TABLE_NAME
        const val ID = "id"
        const val CURRENCY = "currency"
        const val VALUE = "value"
    }

}

