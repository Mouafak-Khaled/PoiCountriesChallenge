package com.example.poicountries.ui.countrydetailsscreen

import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.ImageView
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poicountries.data.Country
import com.example.poicountries.data.api.CountryRepository
import com.squareup.picasso.Picasso
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

import javax.inject.Inject
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import com.squareup.picasso.Target

@HiltViewModel
class CountryDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle, private val countryRepository: CountryRepository
) : ViewModel() {

    private val _country: Country? = savedStateHandle["country"]
    private val country: Country get() = _country!!

    private var originalCountryList: List<Country>? = null


//    private val _primaryColor = MutableLiveData<Int>()
//    val primaryColor: LiveData<Int> get() = _primaryColor
//
//    private val _darkColor = MutableLiveData<Int>()
//    val darkColor: LiveData<Int> get() = _darkColor


    private val _flagBitmap = MutableLiveData<Bitmap?>()
    val flagBitmap: LiveData<Bitmap?> get() = _flagBitmap


    init {
        fetchCountriesFromRepository()
    }

    /**
     * Fetches the list of countries from the repository.
     *
     * This function launches a coroutine in the ViewModel's scope to fetch the list of countries
     * asynchronously. It calls the repository's getCountries() method to fetch the list of countries
     * and assigns it to the originalCountryList.
     */
    private fun fetchCountriesFromRepository() {

        viewModelScope.launch {
            originalCountryList = countryRepository.getCountries()
        }
    }

    /**
     * Loads the flag image of the country into the specified ImageView using Picasso and updates the _bitmap.
     *
     * @param view The ImageView in which the flag image will be loaded.
     */
    fun loadFlagImage(imageView: ImageView) {
        val imageUrl = country.getFlagURL()
        Picasso.get().load(imageUrl).into(object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                imageView.setImageBitmap(bitmap)
                _flagBitmap.value = bitmap
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                e?.printStackTrace()
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }
        })
    }


    /**
     * Formats the area information of the country into a SpannableStringBuilder.
     *
     * @return A SpannableStringBuilder containing the formatted area information.
     */
    fun formatAreaString(): SpannableStringBuilder {
        val nameIdentifier = SpannableString("Area:")
        nameIdentifier.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            nameIdentifier.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return SpannableStringBuilder().append(nameIdentifier).append(" ")
            .append("${country.area} km\u00B2")
    }

    /**
     * Formats the languages spoken in the country into a SpannableStringBuilder.
     *
     * @return A SpannableStringBuilder containing the formatted languages information.
     */
    fun formatLanguagesString(): SpannableStringBuilder {
        val nameIdentifier = SpannableString("Languages:")
        nameIdentifier.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            nameIdentifier.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return SpannableStringBuilder().append(nameIdentifier).append(" ")
            .append(country.getLanguagesList().joinToString(separator = ", "))
    }

    /**
     * Formats the border countries of the country into a SpannableStringBuilder.
     *
     * @return A SpannableStringBuilder containing the formatted border countries information.
     */
    fun formatBordersString(): SpannableStringBuilder {
        val nameIdentifier = SpannableString("Border Countries:")
        nameIdentifier.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            nameIdentifier.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val countryBorders: List<String> = getCountryBorders(country)
        val stringBuilder = SpannableStringBuilder().append(nameIdentifier).append(" ")
        countryBorders.let {
            stringBuilder.append(it.joinToString(separator = ", "))
        }
        return stringBuilder
    }

    /**
     * Gets the formatted name of the country.
     *
     * @return A SpannableStringBuilder containing the formatted country name.
     */
    fun getCountryName(): SpannableStringBuilder {
        val nameIdentifier = SpannableString("Country Name:")
        nameIdentifier.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            nameIdentifier.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return SpannableStringBuilder().append(nameIdentifier).append(" ").append(country.getName())
    }

    /**
     * Gets the formatted capital of the country.
     *
     * @return A SpannableStringBuilder containing the formatted capital name.
     */
    fun getCapital(): SpannableStringBuilder {

        val nameIdentifier = SpannableString("Capital:")
        nameIdentifier.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            nameIdentifier.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return SpannableStringBuilder().append(nameIdentifier).append(" ")
            .append(country.getCapitalName() ?: "")

    }

    /**
     * Gets the formatted subregion of the country.
     *
     * @return A SpannableStringBuilder containing the formatted subregion information.
     */
    fun getCountrySubregion(): SpannableStringBuilder {
        val nameIdentifier = SpannableString("Subregion:")
        nameIdentifier.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            nameIdentifier.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return SpannableStringBuilder().append(nameIdentifier).append(" ")
            .append(country.subregion ?: "")
    }

    /**
     * Gets the formatted population of the country.
     *
     * @return A SpannableStringBuilder containing the formatted population information.
     */
    fun getPopulation(): SpannableStringBuilder {
        val nameIdentifier = SpannableString("Population:")
        nameIdentifier.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            nameIdentifier.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return SpannableStringBuilder().append(nameIdentifier).append(" ")
            .append("${country.population}" ?: "")
    }

    /**
     * Gets the content description of the country image.
     *
     * @return A string containing the content description of the country image.
     */
    fun getImageDescription(): String {
        return country.getContentDescription() ?: ""
    }

    /**
     * Retrieves the list of names of the border countries for the specified country.
     *
     * This function filters the originalCountryList to find countries whose country codes
     * match the borders of the specified country. It then maps the filtered list to the names
     * of those countries.
     *
     * @param country The country whose border countries are to be retrieved.
     * @return A list of names of the border countries, or an empty list if no borders are found.
     */
    private fun getCountryBorders(country: Country): List<String> {

        return originalCountryList?.filter { country.borders?.contains(it.countryCodeAlpha) == true }
            ?.map { it.getName() } ?: emptyList()
    }


}
