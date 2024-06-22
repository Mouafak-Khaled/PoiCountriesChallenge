package com.example.poicountries.ui.countrylistscreen

import androidx.recyclerview.widget.DiffUtil
import com.example.poicountries.data.Country

class CountryListDiffCallback : DiffUtil.ItemCallback<Country>() {

    override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean =
        (oldItem == newItem)

    override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean =
        (oldItem.getName() == newItem.getName())
}