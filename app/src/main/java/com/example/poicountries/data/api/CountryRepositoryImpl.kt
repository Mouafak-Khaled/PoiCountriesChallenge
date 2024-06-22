package com.example.poicountries.data.api

import android.util.Log
import com.example.poicountries.data.Country
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CountryRepositoryImpl @Inject constructor(
    private val countryApi: CountryApi
) : CountryRepository {

    override var originalCountryList: List<Country>? = null

    
    override suspend fun getCountries(): List<Country>? {
        return originalCountryList ?: fetchCountriesFromApi().also { countryList ->
            originalCountryList = countryList
        }
    }


    private suspend fun fetchCountriesFromApi(): List<Country>? {

        var countryList: List<Country>? = null
        try {
            val response = countryApi.getCountries()
            if (response.isSuccessful) countryList = response.body()

        } catch (e: HttpException) {
            Log.e("api_error", "Http Exception: ${e.message}")
            e.printStackTrace()

        } catch (e: IOException) {
            Log.e("api_error", "Network Exception: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e("api_error", "Exception: ${e.message}")
            e.printStackTrace()
        }

        return countryList

    }

}