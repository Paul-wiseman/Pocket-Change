package com.wiseman.currencyconverter.util.coroutine

import kotlinx.coroutines.CoroutineDispatcher

interface DispatchProvider {
    fun unConfined(): CoroutineDispatcher

    fun default(): CoroutineDispatcher

    fun io(): CoroutineDispatcher

    fun main(): CoroutineDispatcher
}