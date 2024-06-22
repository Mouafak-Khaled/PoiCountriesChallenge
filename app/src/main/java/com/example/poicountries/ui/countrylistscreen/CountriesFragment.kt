package com.example.poicountries.ui.countrylistscreen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.children

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.poicountries.R
import com.example.poicountries.databinding.DialogFilterOptionsBinding
import com.example.poicountries.databinding.DialogSortingOptionsBinding
import com.example.poicountries.databinding.FragmentMainBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CountriesFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CountriesViewModel by viewModels()

    // SortDialog Binding
    private var _sortDialogBinding: DialogSortingOptionsBinding? = null
    private val sortDialogBinding get() = _sortDialogBinding!!

    // FilterDialog Binding
    private var _filterDialogBinding: DialogFilterOptionsBinding? = null
    private val filterDialogBinding get() = _filterDialogBinding!!

    private var scrollPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {


        initializeDataBinding(inflater, container)
        val view = binding.root


        setToolBar(binding.toolbar)
        val countrySearchView: SearchView? =
            customizedSearchView(binding.toolbar, requireContext(), R.color.gray)

        setSystemBarColor()

        val adapter = initializeAndBindAdapter()
        bindViewModelToLayoutDataBinding()
        addDividerToRecyclerView()

        onCountryListChange(adapter)
        onSearchQueryTextChanged(countrySearchView)


        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireHost() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_menu, menu)

                val sortingMenuItem = menu.findItem(R.id.sorting)
                val filteringMenuItem = menu.findItem(R.id.filtering)

                sortingMenuItem.setActionView(R.layout.sorting_menu_item)
                filteringMenuItem.setActionView(R.layout.filtering_menu_item)

                sortingMenuItem.actionView?.setOnClickListener {
                    displaySortDialog(requireContext())
                }

                filteringMenuItem.actionView?.setOnClickListener {
                    displayFilterDialog(requireContext())
                }


            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }

        }, viewLifecycleOwner)


    }

    override fun onResume() {
        super.onResume()
        viewModel.setCountryListIfSorted()
        viewModel.setCountryListIfFiltered()
        viewModel.setCountryListIfSearched()
        if (scrollPosition != -1) {
            scrollToPosition(scrollPosition)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _filterDialogBinding = null
        _sortDialogBinding = null

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("clicked_item_position", scrollPosition)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            scrollPosition = savedInstanceState.getInt("clicked_item_position")
        }
    }

    /**
     * Sets up the status bar color for the fragment.
     */
    private fun setSystemBarColor() {
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.light_purple)
    }


    /**
     * Sets up the toolbar for the fragment.
     *
     * @param view The view containing the toolbar.
     * @return True if the toolbar is successfully set, false otherwise.
     */
    private fun setToolBar(view: View): Boolean {


        val toolbar: Toolbar? = view.findViewById(R.id.toolbar)
        toolbar?.let {
            (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
            return true
        }
        return false
    }

    /**
     * Customizes the SearchView appearance.
     *
     * @param view The view containing the SearchView.
     * @param context The context used to access resources.
     * @param color The color to set for the hint text.
     * @return The customized SearchView instance, or null if not found.
     */
    @SuppressLint("PrivateResource", "DiscouragedApi")
    private fun customizedSearchView(view: View, context: Context, color: Int): SearchView? {


        val searchView: SearchView? = view.findViewById(R.id.search_view)
        searchView?.let {
            val editTextId =
                it.context.resources.getIdentifier("android:id/search_src_text", null, null)
            val editText = it.findViewById<EditText>(editTextId)

            editText.setHintTextColor(ContextCompat.getColor(context, color))
            editText.setTextColor(
                ContextCompat.getColor(
                    context, com.google.android.material.R.color.m3_ref_palette_black
                )
            )
            return it
        }
        return null
    }

    /**
     * Initializes data binding for the fragment.
     *
     * @param inflater The LayoutInflater used to inflate the layout.
     * @param container The container holding the fragment.
     */
    private fun initializeDataBinding(inflater: LayoutInflater, container: ViewGroup?) {
        _binding ?: let {
            _binding = FragmentMainBinding.inflate(inflater, container, false)
        }
    }

    /**
     * Adds a divider to the RecyclerView.
     */
    private fun addDividerToRecyclerView() {

        val itemDivider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.coutriesRecyclerView.addItemDecoration(itemDivider)
    }

    /**
     * Observes changes in the country list and updates the adapter accordingly.
     *
     * @param adapter The CountriesFragmentAdapter instance that will be updated with the new country list.
     *
     * This function sets up an observer on the ViewModel's country list. When the country list changes,
     * it submits the new list to the adapter, which updates the displayed items in the RecyclerView.
     */
    private fun onCountryListChange(adapter: CountriesFragmentAdapter) {

        viewModel.countryList.observe(viewLifecycleOwner, Observer { countries ->
            countries?.let {
                adapter.submitList(countries)
            }

        })
    }


    /**
     * Initializes the adapter for the RecyclerView and sets up the navigation for item clicks.
     *
     * @return The initialized CountriesFragmentAdapter.
     *
     * This function creates an instance of CountriesFragmentAdapter with a click listener for country items.
     * When a country item is clicked, it navigates to the CountryDetailsFragment.
     * The adapter is then set on the RecyclerView.
     */
    private fun initializeAndBindAdapter(): CountriesFragmentAdapter {
        val adapter = CountriesFragmentAdapter { country ->
            country?.let {
                viewModel.countryList.value?.indexOf(country)?.let { scrollPosition = it }
                val action =
                    CountriesFragmentDirections.actionMainFragmentToCountryDetailsFragment(country)
                this.findNavController().navigate(action)

            }
        }
        binding.coutriesRecyclerView.adapter = adapter
        return adapter
    }

    /**
     * Binds the ViewModel to the layout's data binding and sets the lifecycle owner.
     *
     * This function assigns the ViewModel to the layout's data binding variable
     * and sets the lifecycle owner to the fragment's view lifecycle owner.
     */
    private fun bindViewModelToLayoutDataBinding() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }

    /**
     * Sets up the SearchView to listen for query text changes and updates the ViewModel with the new search query.
     *
     * @param searchView The SearchView instance where the query text changes are being listened to.
     *
     * This function attaches a listener to the SearchView that updates the ViewModel's search query
     * whenever the text in the SearchView changes. It also observes changes to the ViewModel's search query
     * and triggers a search for countries when the query is updated.
     */
    private fun onSearchQueryTextChanged(searchView: SearchView?) {

        if (searchView == null) return
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { text ->
                    viewModel.setSearchQuery(text)
                }
                return false
            }
        })

        viewModel.searchQuery.observe(viewLifecycleOwner, Observer { query ->
            if (query.isNullOrEmpty()) {
                viewModel.countryList.value?.let { countries ->
                    // Scroll to the first item if the list is not empty
                    if (countries.isNotEmpty()) scrollToPosition(position = 0)
                }
            }
            viewModel.searchForCountry()

        })
    }

    /**
     * Displays the sort dialog.
     *
     * @param context The context used to inflate the dialog layout and create the dialog.
     *
     * This function inflates the custom layout for the sort dialog, creates the dialog,
     * sets up the listener for when the dialog is shown, and then displays the dialog.
     */
    private fun displaySortDialog(context: Context) {
        // Inflate the custom layout
        _sortDialogBinding = DialogSortingOptionsBinding.inflate(LayoutInflater.from(context))
        val dialogView = sortDialogBinding.root

        val sortDialog = createDialog(dialogView, requireContext())

        sortDialog.setOnShowListener {
            handleSortDialogDisplayed(sortDialog)
        }

        sortDialog.show()
    }

    /**
     * Creates a custom AlertDialog with the provided view.
     *
     * @param dialogView The custom view to set in the dialog.
     * @param context The context used to build the dialog.
     * @return The created AlertDialog instance.
     *
     * This function builds an AlertDialog using MaterialAlertDialogBuilder, sets the custom view,
     * and defines the buttons (Apply, Cancel, Clear) without assigning click listeners.
     */
    private fun createDialog(dialogView: View, context: Context): AlertDialog {


        return MaterialAlertDialogBuilder(context).setView(dialogView)
            .setPositiveButton("Apply", null).setNegativeButton("Cancel", null)
            .setNeutralButton("Clear", null).create()

    }

    /**
     * Sets the text color for the buttons in the dialog.
     *
     * @param sortDialog The AlertDialog instance containing the buttons.
     * @param context The context used to access color resources.
     *
     * This function sets the text color of the positive, neutral, and negative buttons in the dialog to a specific color.
     */
    private fun setDialogButtonsTextColor(sortDialog: AlertDialog, context: Context) {

        sortDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(context.getColor(R.color.light_purple))
        sortDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            .setTextColor(context.getColor(R.color.light_purple))
        sortDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(context.getColor(R.color.light_purple))

    }

    /**
     * Handles the Apply button click event in the sort dialog.
     *
     * @param sortDialog The AlertDialog instance representing the sort dialog.
     *
     * This function retrieves the selected sorting options from the dialog, updates the
     * ViewModel with the sorting criteria, and then dismisses the dialog.
     */
    private fun handleSortDialogApplyButtonClick(
        sortDialog: AlertDialog
    ) {
        val sortByTextInput = when {
            sortDialogBinding.countryNameBtn.isChecked -> sortDialogBinding.countryNameBtn.text.toString()
            sortDialogBinding.countryPopulationBtn.isChecked -> sortDialogBinding.countryPopulationBtn.text.toString()
            else -> null
        }

        val isDescending = when {
            sortDialogBinding.sortDescBtn.isChecked -> true
            sortDialogBinding.sortAscBtn.isChecked -> false
            else -> null
        }

        viewModel.sortCountryList(sortByTextInput, isDescending)

        // Dismiss the dialog after applying sorting
        sortDialog.dismiss()
    }

    /**
     * Handles the Clear button click event in the sort dialog.
     *
     * @param context The context used to access resources and set button colors.
     * @param sortByButtonGroup The button group for sorting by options.
     * @param sortOptionsButtonGroup The button group for sorting order options.
     * @param prevSortByCheckedButtonId The ID of the previously checked sort by button.
     * @param prevSortOptionCheckedButtonId The ID of the previously checked sort option button.
     *
     * This function clears the selected sorting options and resets the button styles.
     */
    @SuppressLint("PrivateResource")
    private fun handleSortDialogClearButtonClick(
        context: Context,
        sortByButtonGroup: MaterialButtonToggleGroup,
        sortOptionsButtonGroup: MaterialButtonToggleGroup,
        prevSortByCheckedButtonId: Int,
        prevSortOptionCheckedButtonId: Int
    ) {

        sortByButtonGroup.clearChecked()
        sortOptionsButtonGroup.clearChecked()

        if (prevSortByCheckedButtonId != View.NO_ID) {
            val prevButton =
                sortByButtonGroup.findViewById<MaterialButton>(prevSortByCheckedButtonId)
            prevButton.setBackgroundColor(context.getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color))
            prevButton.setTextColor(context.getColor(R.color.light_purple))
        }

        if (prevSortOptionCheckedButtonId != View.NO_ID) {
            val prevButton = sortOptionsButtonGroup.findViewById<MaterialButton>(
                prevSortOptionCheckedButtonId
            )
            prevButton.setBackgroundColor(context.getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color))
            prevButton.setTextColor(context.getColor(R.color.light_purple))
        }


    }

    /**
     * Sets up the button checked listener for a button group in the sort dialog.
     *
     * @param context The context used to access resources and set button colors.
     * @param buttonGroup The button group to set the listener on.
     * @param isChecked Whether the button is checked.
     * @param checkedId The ID of the checked button.
     * @param prevCheckedButtonId The ID of the previously checked button.
     * @param setPrevCheckedId A function to update the ID of the previously checked button.
     *
     * This function updates the button styles and tracks the checked button within the button group.
     */
    @SuppressLint("PrivateResource")
    private fun setupSortButtonCheckedListener(
        context: Context,
        buttonGroup: MaterialButtonToggleGroup,
        isChecked: Boolean,
        checkedId: Int,
        prevCheckedButtonId: Int,
        setPrevCheckedId: (Int) -> Unit
    ) {


        if (isChecked) {

            if (checkedId != View.NO_ID && checkedId != prevCheckedButtonId) {

                if (prevCheckedButtonId != View.NO_ID) {

                    val btn = buttonGroup.findViewById<MaterialButton>(
                        prevCheckedButtonId
                    )
                    btn.setBackgroundColor(context.getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color))
                    btn.setTextColor(context.getColor(R.color.light_purple))
                }
            }

            val btn = buttonGroup.findViewById<MaterialButton>(checkedId)
            btn.setBackgroundColor(context.getColor(R.color.light_purple))
            btn.setTextColor(context.getColor(R.color.white))

            setPrevCheckedId(checkedId)


        }


    }

    /**
     * Handles the event when the sort dialog is displayed.
     *
     * @param sortDialog The AlertDialog instance representing the sort dialog.
     *
     * This function sets up the initial state of the sort dialog by observing ViewModel properties,
     * setting up button listeners, and defining click listeners for the dialog buttons.
     */
    private fun handleSortDialogDisplayed(
        sortDialog: AlertDialog,

        ) {


        val sortByButtonsGroup: MaterialButtonToggleGroup = sortDialogBinding.countrySortByOptions

        val sortOptionsButtonsGroup: MaterialButtonToggleGroup = sortDialogBinding.sortOptions

        var sortByCheckedButtonId = sortByButtonsGroup.checkedButtonId

        var sortOptionCheckedButtonId = sortOptionsButtonsGroup.checkedButtonId

        viewModel.recentSortingPreference.observe(
            viewLifecycleOwner,
            Observer { recentSortingOption ->


                viewModel.currentlySorted.observe(
                    viewLifecycleOwner,
                    Observer { isCurrentlySorted ->

                        if (isCurrentlySorted) {

                            val sortByOptionText: String = recentSortingOption?.sortFeature ?: ""
                            val isDescending: Boolean = recentSortingOption?.isDescending ?: false


                            if (sortDialogBinding.countryNameBtn.text.toString()
                                    .compareTo(sortByOptionText, ignoreCase = true) == 0
                            ) {

                                sortDialogBinding.countryNameBtn.isChecked = true
                                sortByCheckedButtonId = sortDialogBinding.countryNameBtn.id
                                sortDialogBinding.countryNameBtn.setBackgroundColor(
                                    requireContext().getColor(R.color.light_purple)
                                )
                                sortDialogBinding.countryNameBtn.setTextColor(
                                    requireContext().getColor(
                                        R.color.white
                                    )
                                )

                            } else {
                                sortDialogBinding.countryPopulationBtn.isChecked = true
                                sortByCheckedButtonId = sortDialogBinding.countryPopulationBtn.id
                                sortDialogBinding.countryPopulationBtn.setBackgroundColor(
                                    requireContext().getColor(R.color.light_purple)
                                )
                                sortDialogBinding.countryPopulationBtn.setTextColor(
                                    requireContext().getColor(R.color.white)
                                )
                            }

                            if (isDescending) {
                                sortDialogBinding.sortDescBtn.isChecked = true
                                sortOptionCheckedButtonId = sortDialogBinding.sortDescBtn.id
                                sortDialogBinding.sortDescBtn.setBackgroundColor(
                                    requireContext().getColor(
                                        R.color.light_purple
                                    )
                                )
                                sortDialogBinding.sortDescBtn.setTextColor(
                                    requireContext().getColor(
                                        R.color.white
                                    )
                                )
                            } else {

                                sortDialogBinding.sortAscBtn.isChecked = true
                                sortOptionCheckedButtonId = sortDialogBinding.sortAscBtn.id
                                sortDialogBinding.sortAscBtn.setBackgroundColor(
                                    requireContext().getColor(
                                        R.color.light_purple
                                    )
                                )
                                sortDialogBinding.sortAscBtn.setTextColor(
                                    requireContext().getColor(
                                        R.color.white
                                    )
                                )

                            }


                        }

                    })


            })


        sortByButtonsGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            setupSortButtonCheckedListener(
                requireContext(),
                group,
                isChecked,
                checkedId,
                sortByCheckedButtonId,
            ) { newCheckedId ->
                sortByCheckedButtonId = newCheckedId
            }

        }


        sortOptionsButtonsGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->

            setupSortButtonCheckedListener(
                requireContext(),
                group,
                isChecked,
                checkedId,
                sortOptionCheckedButtonId,
            ) { newId ->
                sortOptionCheckedButtonId = newId
            }

        }

        setDialogButtonsTextColor(sortDialog, requireContext())

        sortDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            handleSortDialogApplyButtonClick(
                sortDialog
            )

            scrollToPosition(position = 0)
            sortDialog.dismiss()

        }

        sortDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setOnClickListener { sortDialog.dismiss() }

        sortDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {

            handleSortDialogClearButtonClick(
                requireContext(),
                sortByButtonsGroup,
                sortOptionsButtonsGroup,
                sortByCheckedButtonId,
                sortOptionCheckedButtonId

            )

        }
    }

    /**
     * Shows the filter dialog.
     *
     * @param context The context used to inflate the dialog layout and create the dialog.
     *
     * This function inflates the filter dialog layout, creates the dialog, sets up the listener
     * for when the dialog is shown, and then displays the dialog.
     */
    private fun displayFilterDialog(context: Context) {
        _filterDialogBinding = DialogFilterOptionsBinding.inflate(LayoutInflater.from(context))
        val dialogView = filterDialogBinding.root
        val filterDialog = createDialog(dialogView, context)

        filterDialog.setOnShowListener { handleFilterDialogDisplayed(filterDialog) }
        filterDialog.show()

    }

    /**
     * Handles actions when the filter dialog is displayed.
     *
     * @param filterDialog The AlertDialog instance representing the filter dialog.
     *
     * This function sets up the initial state of the filter dialog by observing ViewModel properties,
     * setting the text color of the dialog buttons, and defining click listeners for the buttons.
     */
    private fun handleFilterDialogDisplayed(
        filterDialog: AlertDialog,

        ) {

        val checkButtonGroup = filterDialogBinding.subregionsGroup

        viewModel.recentFilterOptions.observe(viewLifecycleOwner) { recentFilterOptions ->

            viewModel.currentlyFiltered.observe(viewLifecycleOwner) { isCurrentlyFiltered ->
                if (isCurrentlyFiltered && !recentFilterOptions.isNullOrEmpty()) {
                    checkButtonGroup.children.filterIsInstance<MaterialCheckBox>()
                        .forEach { checkBoxButton ->
                            if (recentFilterOptions.contains(checkBoxButton.text.toString())) {
                                checkBoxButton.isChecked = true
                            }
                        }
                }
            }
        }



        setDialogButtonsTextColor(filterDialog, requireContext())

        filterDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val subregions = checkButtonGroup.children.filterIsInstance<MaterialCheckBox>()
                .filter { it.isChecked }.map { it.text.toString() }.toList()

            viewModel.filterCountriesBySubregion(subregions)
            scrollToPosition(position = 0)
            filterDialog.dismiss()
        }

        filterDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setOnClickListener { filterDialog.dismiss() }

        filterDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {

            checkButtonGroup.children.filterIsInstance<MaterialCheckBox>()
                .forEach { it.isChecked = false }
        }


    }

    private fun scrollToPosition(position: Int = 0) {
        binding.coutriesRecyclerView.post {
            viewModel.countryList.value?.getOrNull(position)?.let { item ->
                binding.coutriesRecyclerView.layoutManager?.scrollToPosition(
                    viewModel.countryList.value?.indexOf(item) ?: 0
                )
            }
        }
    }

}
