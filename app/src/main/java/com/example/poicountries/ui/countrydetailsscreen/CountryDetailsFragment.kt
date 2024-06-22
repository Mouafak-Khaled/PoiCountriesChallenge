package com.example.poicountries.ui.countrydetailsscreen

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.poicountries.databinding.FragmentCountryDetailsBinding
import dagger.hilt.android.AndroidEntryPoint


import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
import com.example.poicountries.R


@AndroidEntryPoint
class CountryDetailsFragment : Fragment() {

    private var _binding: FragmentCountryDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CountryDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        initializeDataBinding(layoutInflater, container)
        bindViewModelToLayoutDataBinding()
        val view = binding.root

        val countryFlagImgView: ImageView = binding.flag
        viewModel.loadFlagImage(countryFlagImgView)

        // Set up an observer to change the system bar color once the image is loaded
        viewModel.flagBitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            if (bitmap != null) {
                setSystemBarColorFromBitmap(bitmap)
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun initializeDataBinding(inflater: LayoutInflater, container: ViewGroup?) {
        _binding ?: let {
            _binding = FragmentCountryDetailsBinding.inflate(inflater, container, false)
        }
    }


    private fun bindViewModelToLayoutDataBinding() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun setSystemBarColorFromBitmap(bitmap: Bitmap) {
        Palette.from(bitmap).generate { palette ->
            palette?.dominantSwatch?.rgb?.let { color ->
                activity?.window?.statusBarColor = color
            }
        }
    }
}