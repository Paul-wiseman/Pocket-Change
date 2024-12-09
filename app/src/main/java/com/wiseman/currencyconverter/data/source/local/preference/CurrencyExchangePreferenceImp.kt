package com.wiseman.currencyconverter.data.source.local.preference

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CurrencyExchangePreferenceImp @Inject constructor(
    @ApplicationContext private val context: Context
) : CurrencyExchangePreference {

    private val sharedPreferences =
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    override fun getTransactionCounter(): Int = sharedPreferences.getInt(TRANSACTION_COUNT_KEY, 0)

    override fun storeTransactionCount(count: Int) {
        withEdit { putInt(TRANSACTION_COUNT_KEY, count) }
    }

    private inline fun withEdit(block: SharedPreferences.Editor.() -> Unit) {
        with(sharedPreferences.edit()) {
            block()
            apply()
        }
    }

    private companion object {
        const val PREFERENCE_NAME = "CURRENCY_EXCHANGE_SHARED_PREFERENCE"
        const val TRANSACTION_COUNT_KEY = "TRANSACTION_COUNT_KEY"
    }
}