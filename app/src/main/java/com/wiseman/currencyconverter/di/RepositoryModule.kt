package com.wiseman.currencyconverter.di

import com.wiseman.currencyconverter.data.repository.CurrencyTypesRepositoryImpl
import com.wiseman.currencyconverter.data.repository.RatesConversionRepositoryImpl
import com.wiseman.currencyconverter.data.source.local.db.database.AccountTypeDataBase
import com.wiseman.currencyconverter.data.source.local.preference.CurrencyExchangePreference
import com.wiseman.currencyconverter.data.source.remote.RatesService
import com.wiseman.currencyconverter.domain.repository.CurrencyTypesRepository
import com.wiseman.currencyconverter.domain.repository.RatesConversionRepository
import com.wiseman.currencyconverter.domain.usecase.CommissionCalculator
import com.wiseman.currencyconverter.domain.usecase.DefaultCommissionCalculator
import com.wiseman.currencyconverter.util.coroutine.DispatchProvider
import com.wiseman.currencyconverter.util.network.NetworkUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideApiService(
        retrofit: Retrofit
    ): RatesService = retrofit.create()

    @Singleton
    @Provides
    fun provideRatesConversionRepository(
        ratesService: RatesService,
        dispatchProvider: DispatchProvider,
        networkUtil: NetworkUtil,
    ): RatesConversionRepository = RatesConversionRepositoryImpl(
        service = ratesService,
        dispatchProvider,
        networkUtil,
    )

    @Singleton
    @Provides
    fun provideCurrencyTypesRepository(
        dataBase: AccountTypeDataBase,
        dispatchProvider: DispatchProvider,
    ): CurrencyTypesRepository = CurrencyTypesRepositoryImpl(
        dataBase,
        dispatchProvider,
    )

    @Singleton
    @Provides
    fun provideCommissionCalculator(
        preference: CurrencyExchangePreference
    ): CommissionCalculator = DefaultCommissionCalculator(preference)
}
