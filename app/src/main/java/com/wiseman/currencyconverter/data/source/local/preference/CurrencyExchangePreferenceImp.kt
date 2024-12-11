package com.wiseman.currencyconverter.data.source.local.preference

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CurrencyExchangePreferenceImp @Inject constructor(
    @ApplicationContext private val context: Context
) : CurrencyExchangePreference {

    private val sharedPreferences by lazy {
        context.getSharedPreferences(
            PREFERENCE_NAME,
            Context.MODE_PRIVATE
        )
    }

    override fun getTransactionCount(): Int = sharedPreferences.getInt(KEY_TRANSACTION_COUNT, 0)

    override fun setTransactionCount(count: Int) {
        withEdit { putInt(KEY_TRANSACTION_COUNT, count) }
    }

    private inline fun withEdit(block: SharedPreferences.Editor.() -> Unit) {
        with(sharedPreferences.edit()) {
            block()
            commit()
        }
    }

    private companion object {
        const val PREFERENCE_NAME = "CURRENCY_EXCHANGE_SHARED_PREFERENCE"
        const val KEY_TRANSACTION_COUNT = "KEY_TRANSACTION_COUNT"
    }
}

