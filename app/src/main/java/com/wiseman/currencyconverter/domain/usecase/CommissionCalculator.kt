package com.wiseman.currencyconverter.domain.usecase

interface CommissionCalculator {
    fun calculateCommission(amount: Double): Double
}