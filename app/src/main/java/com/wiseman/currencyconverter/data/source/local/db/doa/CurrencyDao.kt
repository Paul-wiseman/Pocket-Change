package com.wiseman.currencyconverter.data.source.local.db.doa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wiseman.currencyconverter.data.source.local.db.entity.AccountTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrency(accountTypeEntity: AccountTypeEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateEntity(accountTypeEntity: AccountTypeEntity)

    @Query("SELECT * FROM Account_Table ORDER BY id")
    fun getAllAvailableCurrencies(): Flow<List<AccountTypeEntity>>
}