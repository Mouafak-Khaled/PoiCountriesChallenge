package com.example.poicountries.data.api

import com.example.poicountries.data.Country
import retrofit2.Response
import retrofit2.http.GET

interface CountryApi {

    @GET("region/europe")
    suspend fun getCountries(): Response<List<Country>>

}