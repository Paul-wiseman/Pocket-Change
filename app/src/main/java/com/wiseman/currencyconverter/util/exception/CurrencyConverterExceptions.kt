package com.wiseman.currencyconverter.util.exception

sealed class CurrencyConverterExceptions(message: String) : Exception(message) {
    class NetworkError(message: String) : CurrencyConverterExceptions(message)
    class ParsingError(message: String) : CurrencyConverterExceptions(message)
    class ApiError(message: String) : CurrencyConverterExceptions("Parsing Error: $message")
    data object UnknownError : CurrencyConverterExceptions("Unknown Error")
}


object ErrorMessages {
    const val NETWORK_ERROR = "No Internet connection"
}