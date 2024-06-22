package com.example.poicountries.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName


@Parcelize
data class Country(

    @SerializedName("cca3") val countryCodeAlpha: String,

    @SerializedName("name") val nameDetails: NameDetails, val subregion: String,

    val flags: Map<String, String>,

    val capital: List<String>, val area: Float, val population: Long,

    val languages: Map<String, String>,

    val borders: List<String>?

) : Parcelable {


    fun getName(): String = nameDetails.commonName

    fun getFlagURL(): String? = flags["png"]

    fun getLanguagesList(): List<String> = languages.values.toList()

    fun getCapitalName(): String? = capital.firstOrNull()

    fun getContentDescription(): String? = flags["alt"]
}

@Parcelize
data class NameDetails(
    @SerializedName("common") val commonName: String
) : Parcelable


