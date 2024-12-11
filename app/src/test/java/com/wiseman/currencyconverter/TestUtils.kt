package com.wiseman.currencyconverter

import com.wiseman.currencyconverter.util.coroutine.DispatchProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
object TestUtils {
     val dispatcher = UnconfinedTestDispatcher()
    val testDispatchProvider = object : DispatchProvider {
        override fun unConfined(): CoroutineDispatcher = dispatcher

        override fun default(): CoroutineDispatcher = dispatcher

        override fun io(): CoroutineDispatcher = dispatcher

        override fun main(): CoroutineDispatcher = dispatcher

    }
}