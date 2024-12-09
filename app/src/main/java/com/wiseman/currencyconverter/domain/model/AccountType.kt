package com.wiseman.currencyconverter.domain.model

data class AccountType(
    val id: Int = 0,
    val currency: String,
    val value: Double
)
