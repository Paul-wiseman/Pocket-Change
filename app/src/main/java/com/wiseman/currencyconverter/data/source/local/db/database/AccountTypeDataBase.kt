package com.wiseman.currencyconverter.data.source.local.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wiseman.currencyconverter.data.source.local.db.doa.CurrencyDao
import com.wiseman.currencyconverter.data.source.local.db.entity.CurrencyEntity

@Database(
    entities = [CurrencyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AccountTypeDataBase : RoomDatabase() {
    abstract val dao: CurrencyDao
}