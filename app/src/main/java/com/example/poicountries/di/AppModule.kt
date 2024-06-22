package com.example.poicountries.di

import com.example.poicountries.data.api.CountryApi
import com.example.poicountries.data.api.CountryRepository
import com.example.poicountries.data.api.CountryRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://restcountries.com/v3.1/"

    @Provides
    @Singleton
    fun provideCountryApi(): CountryApi {

        val httpLoggingInterceptor =
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        val okHttpClient = OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS).readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES).build()

        return Retrofit.Builder().baseUrl(AppModule.BASE_URL).addConverterFactory(
            GsonConverterFactory.create()
        ).client(
            okHttpClient
        ).build().create(CountryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCountryRepository(
        countryApi: CountryApi,
    ): CountryRepository {
        return CountryRepositoryImpl(countryApi)
    }
}