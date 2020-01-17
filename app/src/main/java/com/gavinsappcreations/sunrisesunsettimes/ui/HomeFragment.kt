package com.gavinsappcreations.sunrisesunsettimes.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gavinsappcreations.sunrisesunsettimes.databinding.FragmentHomeBinding
import com.gavinsappcreations.sunrisesunsettimes.viewmodels.SharedViewModel

//TODO: publish app to Github, removing Google TimeZone API key first

class HomeFragment : Fragment() {

    //Get reference to sharedViewModel that was already created by MainActivity.
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

        sharedViewModel.triggerOptionsBottomSheetEvent.observe(this, Observer {
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
            OptionsBottomSheetFragment()
                .show(
                    requireActivity().supportFragmentManager,
                    "options_fragment"
                )
        }
    }

}
