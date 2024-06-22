package com.example.poicountries.utils

import com.example.poicountries.data.Country

/**
 * Sorts a list of countries by their name.
 *
 * @param countries The list of countries to be sorted.
 * @param isDescending A boolean indicating whether to sort in descending order. Defaults to false.
 * @return A list of countries sorted by name.
 */
fun sortCountriesByName(
    countries: List<Country>, isDescending: Boolean
): List<Country> {
    return if (isDescending) countries.sortedByDescending { it.getName() }
    else countries.sortedBy { it.getName() }
}

/**
 * Sorts a list of countries by their population.
 *
 * @param countries The list of countries to be sorted.
 * @param isDescending A boolean indicating whether to sort in descending order. Defaults to false.
 * @return A list of countries sorted by population.
 */
fun sortCountriesByPopulation(
    countries: List<Country>, isDescending: Boolean = false
): List<Country> {
    return if (isDescending) countries.sortedByDescending { it.population }
    else countries.sortedBy { it.population }
}

/**
 * Filters a list of countries by their subregion.
 *
 * @param countries The list of countries to be filtered.
 * @param subregions A list of subregions to filter by.
 * @return A list of countries that belong to the specified subregions.
 */
fun filterCountriesBySubregion(countries: List<Country>, subregions: List<String>): List<Country> {
    return countries.filter { it.subregion in subregions }
}

/**
 * Searches for countries by their name, starting with the provided query.
 *
 * @param countries The list of countries to be searched.
 * @param query The search query string.
 * @return A list of countries whose names start with the provided query.
 */
fun searchForCountries(countries: List<Country>, query: String): List<Country> {
    return countries.filter { it.getName().startsWith(query, ignoreCase = true) }
}
