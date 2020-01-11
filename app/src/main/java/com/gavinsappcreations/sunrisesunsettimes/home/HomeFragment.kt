package com.gavinsappcreations.sunrisesunsettimes.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gavinsappcreations.sunrisesunsettimes.SharedViewModel
import com.gavinsappcreations.sunrisesunsettimes.databinding.FragmentHomeBinding
import com.gavinsappcreations.sunrisesunsettimes.options.OptionsBottomSheetFragment


const val REQUEST_PERMISSIONS_LOCATION_ONLY_REQUEST_CODE = 1

//TODO: change colors
//TODO: If user denies permission, add Button in center of UI to bring up permission choice box again
//TODO: make online work
//TODO: make app work with rotation and process death
//TODO: use TimeZone API to fix find UTC offset with DST https://developers.google.com/maps/documentation/timezone/start
//TODO: add loading indicator (test by turning internet speed down in emulator)
//TODO: must show attribution to sunset-sunrise.org with a link to site
//TODO: app name is Sundial
//TODO: what is the point of the suspend keyword when you alway have to use a coroutine context?
//TODO: app icon
//TODO: landscape mode

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    
    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Giving the binding access to the OfflineViewModel
        binding.sharedViewModel = sharedViewModel

        sharedViewModel.showOptionsBottomSheetEvent.observe(this, Observer {
            if (it == true) {
                showOptionsBottomSheet()
                sharedViewModel.doneShowingOptionsBottomSheet()
            }
        })

        return binding.root
    }

    //This shows the OptionsBottomSheet that lets you change location, date, and toggle online/offline fetching.
    private fun showOptionsBottomSheet() {
        OptionsBottomSheetFragment().show(
            requireActivity().supportFragmentManager,
            "options_fragment"
        )
    }

}
