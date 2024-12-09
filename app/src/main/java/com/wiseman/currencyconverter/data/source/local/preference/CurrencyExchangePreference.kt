package com.wiseman.currencyconverter.data.source.local.preference

interface CurrencyExchangePreference {
    fun getTransactionCounter(): Int
    fun storeTransactionCount(count: Int)
}