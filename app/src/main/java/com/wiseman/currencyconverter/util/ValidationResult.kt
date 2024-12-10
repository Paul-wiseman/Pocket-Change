package com.wiseman.currencyconverter.util

sealed class ValidationResult {
    data class Error(val errorMessage: String) : ValidationResult()
    data object Success : ValidationResult()
}