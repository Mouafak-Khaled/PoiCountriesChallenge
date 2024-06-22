package com.example.poicountries.ui.countrylistscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poicountries.data.Country
import com.example.poicountries.data.api.CountryRepository
import com.example.poicountries.utils.*
import com.example.poicountries.utils.Sorting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CountriesViewModel @Inject constructor(
    private val countryRepository: CountryRepository
) : ViewModel() {

    private var _countryList = MutableLiveData<List<Country>>()
    val countryList: LiveData<List<Country>> get() = _countryList

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> get() = _searchQuery

    private val _recentSortingPreference = MutableLiveData<Sorting?>()
    val recentSortingPreference: LiveData<Sorting?> = _recentSortingPreference

    private val _currentlySorted = MutableLiveData(false)
    val currentlySorted: LiveData<Boolean> = _currentlySorted

    private val _recentFilterOptions = MutableLiveData<List<String>>()
    val recentFilterOptions: LiveData<List<String>> = _recentFilterOptions

    private val _currentlyFiltered = MutableLiveData(false)
    val currentlyFiltered: LiveData<Boolean> = _currentlyFiltered

    private var originalCountryList: List<Country>? = null
    private var temporaryList: List<Country>? = null


    init {
        fetchCountriesFromRepository()
    }

    /**
     * Fetches the list of countries from the repository and updates the LiveData.
     *
     * This function launches a coroutine in the ViewModel's scope to fetch the list of countries
     * asynchronously. It calls the repository's getCountries() method to fetch the list of countries and assign it to the originalCountryList.
     * Once the list is obtained, it updates the _countryList LiveData and assigns the list to
     * temporaryList.
     */
    private fun fetchCountriesFromRepository() {

        viewModelScope.launch {
            //TODO: Change this and hide the details inside the repository
            originalCountryList = countryRepository.getCountries()

            originalCountryList?.let { countries ->
                _countryList.value = countries
                temporaryList = countries

            }
        }
    }


    /**
     * Updates the most recent sorting preference.
     *
     * @param sorting The new sorting preference to be set. It can be null if no sorting is applied.
     *
     * This function updates the _recentSortingPreference LiveData with the provided sorting preference,
     * which indicates the current sorting criteria applied to the country list.
     */
    private fun updateRecentSortingPreference(sorting: Sorting?) {
        _recentSortingPreference.value = sorting
    }

    /**
     * Updates the most recent filter options.
     *
     * @param filterOptions The new list of filter options to be set. It can be an empty list if no filters are applied.
     *
     * This function updates the _recentFilterOptions LiveData with the provided filter options,
     * which indicates the current subregion filters applied to the country list.
     */
    private fun updateRecentFilterOptions(filterOptions: List<String>) {
        _recentFilterOptions.value = filterOptions
    }

    /**
     * Filters the country list based on the current search query.
     *
     * This function retrieves the current search query from _searchQuery LiveData and filters the country list
     * to include only countries whose names start with the search query. It also manages the undo stack to allow
     * reverting to the previous state when the search query is cleared.
     */
    fun searchForCountry() {

        _searchQuery.value?.let { query ->

            if (query.isNullOrEmpty()) _countryList.value = temporaryList ?: originalCountryList

            temporaryList?.let { countryList ->
                if (countryList.isNotEmpty()) {
                    _countryList.value = searchForCountries(countryList, query)
                    return
                }
            }
            originalCountryList?.let { countryList ->
                if (countryList.isNotEmpty()) {
                    _countryList.value = searchForCountries(countryList, query)
                    return
                }
            }

        }

    }

    /**
     * Sorts the country list based on the specified feature and order.
     *
     * @param sortFeature The feature to sort by, either "Name" or "Population". If null or unrecognized, no sorting is applied.
     * @param isDescending Whether the sorting should be in descending order. Defaults to false (ascending order).
     *
     * This function retrieves the current country list and sorts it based on the specified feature and order.
     * The sorted list is then updated in _countryList LiveData, and the recent sorting preference is updated accordingly.
     */
    fun sortCountryList(sortFeature: String?, isDescending: Boolean?) {
        val currentList = temporaryList ?: emptyList() // Get the current country list

        // Sort the list based on the specified feature and order
        if (sortFeature != null && isDescending != null) {
            val sortedList =
                if (sortFeature.compareTo("name", ignoreCase = true) == 0) sortCountriesByName(
                    currentList, isDescending
                )
                else sortCountriesByPopulation(currentList, isDescending)

            temporaryList = sortedList
            _countryList.value = sortedList
            updateRecentSortingPreference(
                Sorting(sortFeature, isDescending)
            )
            _currentlySorted.value = true
        } else onSortPreferencesCleared()

        setCountryListIfSearched()

    }

    /**
     * Filters the country list based on the specified subregions.
     *
     * @param subregions The list of subregions to filter by. Only countries belonging to these subregions will be included in the result.
     *
     * This function clears the current search query and filters the country list based on the provided subregions.
     * If the original country list is not empty, the filtered list is updated in _countryList LiveData,
     * and the recent filter options are updated accordingly. The sorting state is also reapplied to the filtered list.
     */
    fun filterCountriesBySubregion(subregions: List<String>) {
        if (subregions.isNotEmpty()) {
            originalCountryList?.let { countryList ->
                // Filter the list based on the provided subregions
                temporaryList = filterCountriesBySubregion(countryList, subregions)
                _countryList.value = temporaryList ?: emptyList()
                _currentlyFiltered.value = true // Set the filtered state to true
                updateRecentFilterOptions(subregions) // Update filter options
                setCountryListIfSorted()
                setCountryListIfSearched()
            }
        } else onFilterPreferencesCleared()
    }


    /**
     * Sets the search query used for filtering the country list.
     *
     * @param query The search query entered by the user.
     *
     * This function updates the value of [_searchQuery] LiveData with the provided search query,
     * triggering filtering of the country list based on the updated query.
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Displays the original list of countries.
     *
     * This function updates the [_countryList] LiveData with the original list of countries retrieved
     * from the repository, effectively displaying the unfiltered list of countries.
     */
    private fun displayOriginalCountryList() {
        originalCountryList?.let {
            _countryList.value = it
            temporaryList = it
        }
    }


    /**
     * Sets the country list if it is sorted based on the recent sorting preference.
     *
     * This function checks if the country list is currently sorted based on the recent sorting preference.
     * If the list is sorted, it retrieves the sorting feature and order from [_recentSortingPreference] LiveData,
     * and then sorts the country list accordingly using [sortCountryList] function.
     */
    fun setCountryListIfSorted() {
        _currentlySorted.value?.let { isCurrentlySorted ->
            if (isCurrentlySorted) {
                val sortFeature: String = _recentSortingPreference.value?.sortFeature ?: ""
                val isDescending: Boolean = _recentSortingPreference.value?.isDescending ?: false
                sortCountryList(sortFeature, isDescending)
            }
        }
    }


    /**
     * Sets the country list if it was filtered.
     *
     * This function checks if the country list was filtered previously.
     * If it was filtered, it reapplies the filter based on the recent filter options.
     * It also restores the search query if it was previously set.
     *
     * @see filterCountriesBySubregion
     * @see searchForCountry
     */
    fun setCountryListIfFiltered() {
        // Check if the country list was previously filtered
        _currentlyFiltered.value?.let { isCurrentlyFiltered ->
            if (isCurrentlyFiltered) {
                // Retrieve the recent filter options
                val subregionsToFilterWith = _recentFilterOptions.value
                if (!subregionsToFilterWith.isNullOrEmpty()) {
                    // Apply the filter based on the recent filter options
                    filterCountriesBySubregion(subregionsToFilterWith)
                }
            }
        }

    }


    /**
     * Handles the event when sorting preferences are cleared.
     *
     * This function resets the sorting state by setting [_currentlySorted] LiveData to false
     * and updating the recent sorting preference to null using [updateRecentSortingPreference] function.
     * It then displays the original country list and reapplies any active filter options if filtering was applied.
     */
    private fun onSortPreferencesCleared() {
        _currentlySorted.value = false
        updateRecentSortingPreference(null)
        displayOriginalCountryList()
        _currentlyFiltered.value?.let { currFiltered ->
            if (currFiltered) {
                _recentFilterOptions.value?.let { subregions ->
                    filterCountriesBySubregion(subregions)
                }
            }
        }
    }

    /**
     * Handles the event when filter preferences are cleared.
     *
     * This function resets the filtering state by setting [_currentlyFiltered] LiveData to false
     * and updating the recent filter options to an empty list using [updateRecentFilterOptions] function.
     * It then displays the original country list and reapplies any active sorting if sorting was applied.
     */
    private fun onFilterPreferencesCleared() {
        _currentlyFiltered.value = false
        updateRecentFilterOptions(emptyList())
        displayOriginalCountryList()
        _currentlySorted.value?.let { currSorted ->
            if (currSorted) {
                _recentSortingPreference.value?.let { sorting ->
                    sortCountryList(sorting.sortFeature, sorting.isDescending)
                }
            }
        }
    }

    /**
     * Sets the country list if it was searched previously.
     *
     * This function retrieves the search query from the saved state handle and checks if a search was performed.
     * If a search was performed, it restores the search query and updates the country list accordingly.
     * If the search query is not empty, it searches for countries based on the query.
     */
    fun setCountryListIfSearched() {

        val searchQuery = _searchQuery.value

        if (!searchQuery.isNullOrEmpty()) {
            searchForCountry()
        }

    }


}
