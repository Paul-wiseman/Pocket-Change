package com.wiseman.currencyconverter.data.source.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wiseman.currencyconverter.util.Constants.CURRENCY_ENTITY_TABLE_NAME

@Entity(tableName = CURRENCY_ENTITY_TABLE_NAME)
data class CurrencyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val currency: String,
    val value: Double
)
