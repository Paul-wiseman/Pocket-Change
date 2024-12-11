package com.wiseman.currencyconverter.util.exception

sealed class CurrencyConverterExceptions(message: String) : Exception(message) {
    class NetworkError(message: String) : CurrencyConverterExceptions(message)
    class ApiError(message: String) : CurrencyConverterExceptions(message)
}


object ErrorMessages {
    const val NETWORK_ERROR = "Unable to connect to the server. Please check your internet connection and try again"
    const val API_ERROR = "A network error occurred."
    const val INVALID_RESPONSE = "Invalid API response."
}
