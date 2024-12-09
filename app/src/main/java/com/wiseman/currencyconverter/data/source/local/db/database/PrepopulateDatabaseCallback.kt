package com.wiseman.currencyconverter.data.source.local.db.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wiseman.currencyconverter.data.source.local.db.entity.AccountTypeEntity
import com.wiseman.currencyconverter.util.Constants.CURRENCY_ENTITY_TABLE_NAME

class PrepopulateDatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        val accountTypes = listOf(
            AccountTypeEntity(100, "EUR", 1000.00),
            AccountTypeEntity(1001, "USD", 0.00),
            AccountTypeEntity(1002, "GBP", 0.00)
        )
        accountTypes.forEach { accountType ->
            db.execSQL(
                "INSERT INTO $CURRENCY_ENTITY_TABLE_NAME (id, currency, value) VALUES (?, ?, ?)",
                arrayOf(accountType.id, accountType.currency, accountType.value)
            )
        }
    }
}