package com.gavinsappcreations.sunrisesunsettimes.options

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gavinsappcreations.sunrisesunsettimes.PLACES_API_KEY
import com.gavinsappcreations.sunrisesunsettimes.R
import com.gavinsappcreations.sunrisesunsettimes.SharedViewModel
import com.gavinsappcreations.sunrisesunsettimes.databinding.OptionsBottomSheetLayoutBinding
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*

//BottomSheetDialogFragment that uses a custom theme which sets a rounded background
class OptionsBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: OptionsBottomSheetLayoutBinding

    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)
    }

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {

        binding = OptionsBottomSheetLayoutBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.sharedViewModel = sharedViewModel

        Places.initialize(
            requireActivity().application,
            PLACES_API_KEY
        )

        //When a change is made in the locationRadioGroup, save the new selection to viewModel
        binding.locationRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val usingCustomLocation = checkedId == R.id.custom_location_radioButton
            sharedViewModel.onUsingCustomLocationChanged(usingCustomLocation)

            /*
            * If using current location, set place equal to null as this will force
            * FusedLocationProvider to fetch user's current location.
            */
            if (!usingCustomLocation && sharedViewModel.place.value != null) {
                sharedViewModel.onPlaceChanged(null)
            }
        }

        //Create a Calendar instance and initialize it to the correct date
        val calendar: Calendar = Calendar.getInstance()
        val timeInMillis = sharedViewModel.dateInMillis.value
        timeInMillis?.let {
            calendar.timeInMillis = it
        }

        //initialize the datePicker and define the onDateChangedListener
        binding.datePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            sharedViewModel.onDateChanged(calendar.timeInMillis)
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //force BottomSheet to start fully expanded
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog?
            val bottomSheet: FrameLayout? =
                dialog!!.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }

        //initialize the AutocompleteSupportFragment
        val autocompleteFragment =
            requireActivity().supportFragmentManager
                .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        //specify the types of place data to return
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.UTC_OFFSET
            )
        )
        autocompleteFragment.setTypeFilter(TypeFilter.CITIES)
        autocompleteFragment.setHint(getString(R.string.city))
        val clearButton =
            autocompleteFragment.view!!.findViewById<ImageButton>(R.id.places_autocomplete_clear_button)
        //If user presses autocompleteFragment's clearButton, set place = null
        clearButton.setOnClickListener {
            sharedViewModel.onPlaceChanged(null)
        }

        //hide cursor from autocompleteFragment's editText, as it also prevents pasting
        val editText = autocompleteFragment.view!!
            .findViewById<EditText>(R.id.places_autocomplete_search_input)
        editText.isCursorVisible = false

        sharedViewModel.place.observe(this, Observer { place ->
            place?.let {
                val placeName = place.name
                if (placeName == getString(R.string.current_location)) {
                    autocompleteFragment.setText("")
                } else {
                    autocompleteFragment.setText(place.name)
                }
            }
        })

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                sharedViewModel.onPlaceChanged(place)
            }

            override fun onError(status: Status) {
                Log.i("LOG", "An error occurred: " + status);
            }
        })
    }


    override fun onDestroyView() {
        //Handle case when user selects "custom location" but doesn't enter a location.
        if (sharedViewModel.usingCustomLocation.value == true
            && sharedViewModel.place.value?.name == getString(R.string.current_location)
        ) {
            sharedViewModel.onUsingCustomLocationChanged(false)
            Toast.makeText(
                requireContext(),
                "No custom location entered. Reverting to current location.",
                Toast.LENGTH_LONG
            ).show()
        }

        /*
        * We need to remove the autocompleteFragment here since it doesn't remove itself
        * (as it wasn't created programmatically)
        */
        val fm = requireActivity().supportFragmentManager
        val autocompleteFragment: Fragment = fm
            .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        fm.beginTransaction().remove(autocompleteFragment).commit()

        super.onDestroyView()
    }

    //Create BottomSheet
    override fun onCreateDialog(savedInstanceState: Bundle?) =
        BottomSheetDialog(requireContext(), theme)

    //Set custom theme on BottomSheet
    override fun getTheme(): Int = R.style.BottomSheetTheme

}