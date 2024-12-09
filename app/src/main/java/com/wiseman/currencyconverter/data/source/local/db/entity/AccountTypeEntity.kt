package com.wiseman.currencyconverter.data.source.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wiseman.currencyconverter.util.Constants.CURRENCY_ENTITY_TABLE_NAME

@Entity(tableName = CURRENCY_ENTITY_TABLE_NAME)
data class AccountTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val currency: String = "USD",
    val value: Double = 0.00
)
