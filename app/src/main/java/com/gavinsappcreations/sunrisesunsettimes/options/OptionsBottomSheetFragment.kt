package com.gavinsappcreations.sunrisesunsettimes.options

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gavinsappcreations.sunrisesunsettimes.R
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


const val PLACES_API_KEY = "AIzaSyCiNoSDVQtYBByS97Mou3v0k3o_1hR38qE"

//BottomSheetDialogFragment that uses a custom theme which sets a rounded background
class OptionsBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: OptionsBottomSheetLayoutBinding

    private val viewModel: OptionsViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(this, OptionsViewModelFactory(activity.application))
            .get(OptionsViewModel::class.java)
    }

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {

        binding = OptionsBottomSheetLayoutBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        Places.initialize(
            requireActivity().application,
            PLACES_API_KEY
        )

        //When a change is made in the locationRadioGroup, save the new selection to viewModel
        binding.locationRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val usingCurrentLocation = checkedId == R.id.current_location_radioButton
            viewModel.updateUsingCurrentLocation(usingCurrentLocation)
        }


        //TODO: instead of this, use two-way databinding and a MediatorLiveData to combine these into one emitter
        val calendar: Calendar = Calendar.getInstance()
        binding.datePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog?
            val bottomSheet: FrameLayout? =
                dialog!!.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            requireActivity().supportFragmentManager
                .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.UTC_OFFSET
            )
        )
        autocompleteFragment.setTypeFilter(TypeFilter.CITIES)
        autocompleteFragment.setHint("City")
        val editText = autocompleteFragment.view!!
            .findViewById<EditText>(R.id.places_autocomplete_search_input)
        editText.isCursorVisible = false

        viewModel.place.observe(this, Observer {
            val placeName = it.name
            if (placeName == getString(R.string.current_location)) {
                autocompleteFragment.setText("")
            } else {
                autocompleteFragment.setText(it.name)
            }
        })

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                viewModel.updateLocation(place)
            }

            override fun onError(status: Status) {
                Log.i("LOG", "An error occurred: " + status);
            }

        })
    }


    //We need to remove the autocompleteFragment here since it doesn't remove itself
    //(as it wasn't created programmatically)
    override fun onDestroyView() {
        super.onDestroyView()
        val fm = requireActivity().supportFragmentManager
        val autocompleteFragment: Fragment = fm
            .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        fm.beginTransaction().remove(autocompleteFragment).commit()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        BottomSheetDialog(requireContext(), theme)

    override fun getTheme(): Int = R.style.BottomSheetTheme

}