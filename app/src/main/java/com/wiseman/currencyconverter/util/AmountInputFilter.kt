package com.wiseman.currencyconverter.util

import android.text.InputFilter
import android.text.Spanned


class AmountInputFilter : InputFilter {
    private val regex = Regex("^[0-9]+(\\.[0-9]{0,2})?\$")
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val input = dest.toString().substring(0, dstart) + source.toString()
            .substring(start, end) + dest.toString()
            .substring(dend)
        return if (regex.matches(input)) {
            null
        } else {
            ""
        }
    }
}
