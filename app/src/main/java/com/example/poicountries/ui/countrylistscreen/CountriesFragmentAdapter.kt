package com.example.poicountries.ui.countrylistscreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.poicountries.data.Country
import com.example.poicountries.databinding.CountryItemBinding
import com.squareup.picasso.Picasso

class CountriesFragmentAdapter(private val clickListener: (country: Country) -> Unit) :
    ListAdapter<Country, CountriesFragmentAdapter.CountryViewHolder>(
        CountryListDiffCallback()
    ) {

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val country = getItem(position)
        Picasso.get().load(country.getFlagURL()).into(holder.binding.flag);

        holder.bind(country, clickListener)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): CountryViewHolder = CountryViewHolder.inflateFrom(parent)


    class CountryViewHolder(val binding: CountryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {

            fun inflateFrom(parent: ViewGroup): CountryViewHolder {

                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CountryItemBinding.inflate(layoutInflater, parent, false)

                return CountryViewHolder(binding)

            }
        }

        fun bind(country: Country, clickListener: (country: Country) -> Unit) {
            binding.country = country
            binding.root.setOnClickListener { clickListener(country) }

        }

    }
}