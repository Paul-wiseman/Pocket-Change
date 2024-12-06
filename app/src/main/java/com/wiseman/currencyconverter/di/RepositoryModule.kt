package com.wiseman.currencyconverter.di

import com.wiseman.currencyconverter.data.repository.RatesConversionRepositoryImpl
import com.wiseman.currencyconverter.data.source.remote.RateRatesService
import com.wiseman.currencyconverter.domain.repository.RatesConversionRepository
import com.wiseman.currencyconverter.util.coroutine.DispatchProvider
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
    ): RateRatesService = retrofit.create()

    @Singleton
    @Provides
    fun provideAvailablePropertiesRepository(
        ratesService: RateRatesService,
        dispatchProvider: DispatchProvider
    ): RatesConversionRepository = RatesConversionRepositoryImpl(
        service = ratesService,
        dispatchProvider
    )
}