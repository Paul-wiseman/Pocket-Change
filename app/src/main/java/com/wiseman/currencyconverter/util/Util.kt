package com.wiseman.currencyconverter.util

import android.content.Context
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wiseman.currencyconverter.R
import com.wiseman.currencyconverter.util.Constants.TAG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.Locale


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

inline fun <reified T : Parcelable> Bundle.getTypedParcelable(parcelableKey: String): T? = when {
    SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(parcelableKey, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(parcelableKey) as? T
}

/**
 * Shows an error dialog with a title and message.
 *
 * This function creates and displays an AlertDialog with an error theme,
 * showing the provided title and message. The dialog has an "OK" button
 * to dismiss it.
 *
 * @param context The context used to create the dialog.
 * @param title The title of the dialog.
 * @param message The message to display in the dialog.
 */
fun showErrorDialog(context: Context, title: String, message: String) {
    val dialog = AlertDialog.Builder(context, R.style.ErrorDialogTheme)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        .create()

    dialog.show()

    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        .setTextColor(ContextCompat.getColor(context, R.color.dark_blue))
}

fun Double.formatToTwoDecimalString(): String {
    return try {
        val decimalFormat = DecimalFormat("#.##")
        return decimalFormat.format(this)
    } catch (e: NumberFormatException) {
        Log.e(TAG, "failed to format number to 2 decimal place: ", )
        this.toString()
    }
}

fun Double.roundToTwoDecimalPlaces(): Double {
    return String.format(
        Locale.getDefault(),
        "%.2f", this).toDouble()
}
