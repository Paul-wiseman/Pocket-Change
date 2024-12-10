package com.wiseman.currencyconverter.data.source.local.preference

interface CurrencyExchangePreference {
    fun getTransactionCount(): Int
    fun setTransactionCount(count: Int)
}