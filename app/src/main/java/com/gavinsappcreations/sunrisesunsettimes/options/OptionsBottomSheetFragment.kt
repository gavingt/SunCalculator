package com.gavinsappcreations.sunrisesunsettimes.options

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.gavinsappcreations.sunrisesunsettimes.R
import com.gavinsappcreations.sunrisesunsettimes.databinding.FragmentHomeBinding
import com.gavinsappcreations.sunrisesunsettimes.databinding.OptionsBottomSheetLayoutBinding
import com.gavinsappcreations.sunrisesunsettimes.home.HomeViewModel
import com.gavinsappcreations.sunrisesunsettimes.home.HomeViewModelFactory
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

const val PLACES_API_KEY = "AIzaSyCiNoSDVQtYBByS97Mou3v0k3o_1hR38qE"

//BottomSheetDialogFragment that uses a custom theme which sets a rounded background
class OptionsBottomSheetFragment : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetTheme

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

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Giving the binding access to the OfflineViewModel
        binding.viewModel = viewModel

        // Initialize the SDK
        Places.initialize(
            requireActivity().application,
            PLACES_API_KEY
        )

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog?
            val bottomSheet : FrameLayout? =
                dialog!!.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            requireActivity().supportFragmentManager
                .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.UTC_OFFSET, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS))
        autocompleteFragment.setTypeFilter(TypeFilter.CITIES)
        autocompleteFragment.setHint("City")

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i("LOG", "Place: " + place.name + ", " + place.id)
            }

            override fun onError(status: Status) {
                Log.i("LOG", "An error occurred: " + status);
            }

        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
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


}