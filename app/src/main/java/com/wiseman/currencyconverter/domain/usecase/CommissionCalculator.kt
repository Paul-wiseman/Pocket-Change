package com.wiseman.currencyconverter.domain.usecase

/**
 * An interface for calculating commission based on a given amount.
 * Implementations of this interface provide the logic for
 * determining the commission amount.
 */
interface CommissionCalculator {
    fun calculateCommission(amount: Double): Double
}
