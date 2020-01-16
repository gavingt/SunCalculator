package com.gavinsappcreations.sunrisesunsettimes.options

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gavinsappcreations.sunrisesunsettimes.R
import com.gavinsappcreations.sunrisesunsettimes.SharedViewModel
import com.gavinsappcreations.sunrisesunsettimes.databinding.OptionsBottomSheetLayoutBinding
import com.gavinsappcreations.sunrisesunsettimes.utilities.PLACES_API_KEY
import com.gavinsappcreations.sunrisesunsettimes.utilities.waitForDatePickerToSettle
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

    private var _binding: OptionsBottomSheetLayoutBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding
        get() = _binding!!

    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)
    }

    private lateinit var autocompleteFragment: AutocompleteSupportFragment

    /**
     * This keeps track of the last time the OnDateChangedListener was called. This enables us
     * to wait for the DatePicker spinner to settle, which eliminates unnecessary API calls.
     */
    private var timeDateChangedInMillis = System.currentTimeMillis()

    //We need a single Handler so we can remove old callbacks while waiting for the DatePicker to settle.
    private val dateChangedHandler = Handler()


    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {

        _binding = OptionsBottomSheetLayoutBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.sharedViewModel = sharedViewModel

        Places.initialize(
            requireActivity().application,
            PLACES_API_KEY
        )

        //When a change is made in the locationRadioGroup, save the new selection to viewModel
        binding.locationRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val usingCustomLocationOldValue = sharedViewModel.usingCustomLocation.value
            val usingCustomLocationNewValue = checkedId == R.id.custom_location_radioButton

            //If usingCustomLocation value is unchanged, just return.
            if (usingCustomLocationOldValue == usingCustomLocationNewValue) {
                return@setOnCheckedChangeListener
            }

            sharedViewModel.onUsingCustomLocationChanged(usingCustomLocationNewValue)

            /*
            * If using current location, set place equal to null as this will force
            * FusedLocationProvider to fetch user's current location.
            */
            if (!usingCustomLocationNewValue) {
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
            timeDateChangedInMillis = System.currentTimeMillis()
            sharedViewModel.onDatePickerSettlingChanged(true)
            //Call code in lambda only after DatePicker has settled
            waitForDatePickerToSettle (dateChangedHandler, timeDateChangedInMillis) {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                sharedViewModel.onDateChanged(calendar.timeInMillis)
                sharedViewModel.onDatePickerSettlingChanged(false)
            }
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.viewTreeObserver.addOnGlobalLayoutListener {
            //Force BottomSheet to start fully expanded and set peekHeight=0 so it never peeks
            val dialog = dialog as BottomSheetDialog?
            val bottomSheet: FrameLayout? =
                dialog!!.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.peekHeight = 0

            /**
             * Fix some issues with the default behavior of AutocompleteFragment. Since we're
             * inflating autocompleteFragment programmatically, we need to listen for its Views
             * to be created before we can modify them.
             */
            autocompleteFragment.view?.viewTreeObserver?.addOnGlobalLayoutListener {
                val clearButton =
                    autocompleteFragment.view!!.findViewById<ImageButton>(R.id.places_autocomplete_clear_button)

                //If user presses autocompleteFragment's clearButton, set place = null
                clearButton.setOnClickListener {
                    sharedViewModel.onPlaceChanged(null)
                }

                //hide cursor from autocompleteFragment's editText, as it also prevents pasting
                val autocompleteEditText = autocompleteFragment.view!!
                    .findViewById<EditText>(R.id.places_autocomplete_search_input)
                autocompleteEditText.isCursorVisible = false
            }
        }


        //Inflate the autocompleteFragment
        val fm = childFragmentManager
        val ft = fm.beginTransaction()
        autocompleteFragment = AutocompleteSupportFragment()
        ft.add(R.id.autocomplete_frameLayout, autocompleteFragment, "autocompleteFragment")
        ft.commit()
        fm.executePendingTransactions()

        //Specify the types of place data to return
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.UTC_OFFSET
            )
        )
        autocompleteFragment.setTypeFilter(TypeFilter.CITIES)
        autocompleteFragment.setHint(getString(R.string.city))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                sharedViewModel.onPlaceChanged(place)
            }

            override fun onError(status: Status) {
                Log.i("LOG", "An error occurred: " + status);
            }
        })

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


    }

    //If no place has been selected, detect it here and revert to current location.
    override fun onDestroyView() {

        super.onDestroyView()

        /**
         * Since Fragments outlive their Views, we need to clean up references to the binding
         * class instance when we close the Fragment
         */
        _binding = null

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

        super.onDestroyView()
    }

    //Create BottomSheet
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        return BottomSheetDialog(requireContext(), theme)
    }

    //Set custom theme on BottomSheet
    override fun getTheme(): Int = R.style.BottomSheetTheme

}