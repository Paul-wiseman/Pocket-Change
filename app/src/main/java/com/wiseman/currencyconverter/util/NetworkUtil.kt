package com.wiseman.currencyconverter.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import javax.inject.Inject

/**
 * Checks if the device is currently connected to the internet.
 *
 * This method checks for the presence of an active network connection with
 * capabilities for Wi-Fi, cellular data, or Ethernet.
 *
 * @param Context The application context.
 * @return `true` if the device is connected to the internet, `false` otherwise.
 */

class NetworkUtil @Inject constructor() {
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_CELLULAR
        ) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}