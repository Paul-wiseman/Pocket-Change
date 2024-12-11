package com.wiseman.currencyconverter.di

import android.content.Context
import androidx.room.Room
import com.wiseman.currencyconverter.BuildConfig
import com.wiseman.currencyconverter.data.source.local.db.database.AccountTypeDataBase
import com.wiseman.currencyconverter.data.source.local.db.database.PrepopulateDatabaseCallback
import com.wiseman.currencyconverter.data.source.local.preference.CurrencyExchangePreference
import com.wiseman.currencyconverter.data.source.local.preference.CurrencyExchangePreferenceImp
import com.wiseman.currencyconverter.util.Constants
import com.wiseman.currencyconverter.util.coroutine.DispatchProvider
import com.wiseman.currencyconverter.util.coroutine.DispatchProviderImp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(
            Json.asConverterFactory(
                "application/json; charset=UTF8".toMediaType()
            )
        )
        .build()

    @Singleton
    @Provides
    fun providesScheduler(): DispatchProvider = DispatchProviderImp()

    @Singleton
    @Provides
    fun providePreference(
        @ApplicationContext context: Context
    ): CurrencyExchangePreference = CurrencyExchangePreferenceImp(context)

    @Singleton
    @Provides
    fun providesDatabase(
        @ApplicationContext context: Context,
        dispatchProvider: DispatchProvider
    ): AccountTypeDataBase =
        Room.databaseBuilder(
            context,
            AccountTypeDataBase::class.java,
            Constants.CURRENCY_DATABASE_NAME
        )
            .addCallback(PrepopulateDatabaseCallback(dispatchProvider))
            .build()

}
