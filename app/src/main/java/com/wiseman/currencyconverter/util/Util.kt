package com.wiseman.currencyconverter.util

import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DecimalFormat


/**
 * Collects values from a flow within an activity's lifecycle.
 *
 * This function launches a coroutine that collects values from the
 * flow and executes the provided `onCollect` lambda function with each
 * collected value. The collection is automatically stopped and restarted
 * when the activity's lifecycle state changes between STARTED and STOPPED.
 *
 * @param onCollect The lambda function to execute with each collected
 *     value.
 * @param T The type of values emitted by the flow.
 * @receiver The flow to collect from.
 */

context(AppCompatActivity)
fun <T> Flow<T>.collectInActivity(onCollect: (T) -> Unit) =
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            collectLatest {
                onCollect(it)
            }
        }
    }

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}


fun Double.formatToTwoDecimalString(): String {
    return try {
        val decimalFormat = DecimalFormat("#.##")
        return decimalFormat.format(this)
    } catch (e: Exception) {
        this.toString()
    }
}

fun Double.roundToTwoDecimalPlaces(): Double {
    return String.format("%.2f", this).toDouble()
}


