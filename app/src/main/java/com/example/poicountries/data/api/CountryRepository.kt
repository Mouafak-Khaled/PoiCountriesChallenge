package com.example.poicountries.data.api

import com.example.poicountries.data.Country
import retrofit2.Response


interface CountryRepository {

    var originalCountryList: List<Country>?

    suspend fun getCountries(): List<Country>?

}