package com.gavinsappcreations.sunrisesunsettimes.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gavinsappcreations.sunrisesunsettimes.SharedViewModel
import com.gavinsappcreations.sunrisesunsettimes.databinding.FragmentHomeBinding
import com.gavinsappcreations.sunrisesunsettimes.options.OptionsBottomSheetFragment

//TODO: If user denies permission and closes BottomSheet, show floating snackbar or banner at top
//TODO: add error handling for network timeout (try with really bad internet). Show as floating snackbar with RETRY button.
       //do we handle this as try-catches in the coroutines?
//TODO: sun data sometimes doesn't show up with bad internet
//TODO: move calculations to separate project

class HomeFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentHomeBinding.inflate(inflater)

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
        val fragment =
            requireActivity().supportFragmentManager.findFragmentByTag("options_fragment")

        //Prevent multiple BottomSheets from showing by null checking.
        if (fragment == null) {
            OptionsBottomSheetFragment().show(
                requireActivity().supportFragmentManager,
                "options_fragment"
            )
        }
    }

}
