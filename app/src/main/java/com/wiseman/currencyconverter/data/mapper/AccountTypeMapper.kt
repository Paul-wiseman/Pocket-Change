package com.wiseman.currencyconverter.data.mapper

import com.wiseman.currencyconverter.data.source.local.db.entity.AccountTypeEntity
import com.wiseman.currencyconverter.domain.model.AccountType


fun AccountTypeEntity.toAccountType() = AccountType(
    id = id,
    currency = currency,
    value = value
)

fun AccountType.toAccountTypeEntity() = AccountTypeEntity(
    id = id,
    currency = currency,
    value = value
)