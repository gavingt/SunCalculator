package com.gavinsappcreations.sunrisesunsettimes.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gavinsappcreations.sunrisesunsettimes.BuildConfig
import com.gavinsappcreations.sunrisesunsettimes.databinding.FragmentHomeBinding
import com.gavinsappcreations.sunrisesunsettimes.options.OptionsBottomSheetFragment
import com.gavinsappcreations.sunrisesunsettimes.repository.SunRepository


const val REQUEST_PERMISSIONS_LOCATION_ONLY_REQUEST_CODE = 1

//TODO: change colors
//TODO: make it work when permission denied
//TODO: make online work
//TODO: add datepicker
//TODO: remove offline functionality if it's the same result as online
//TODO: fix edge cases with location picker (leaving it blank, etc...)
//TODO: check dates and places where daylight savings is active, to check for math errors in SolarEventCalculator caused by me changing code

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private var alertDialog: AlertDialog? = null

    private val viewModel: HomeViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(this, HomeViewModelFactory(activity.application))
            .get(HomeViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Giving the binding access to the OfflineViewModel
        binding.viewModel = viewModel

        viewModel.showOptionsBottomSheetEvent.observe(this, Observer {
            if (it == true) {
                showOptionsBottomSheet()
                viewModel.doneShowingOptionsBottomSheet()
            }
        })

        viewModel.usingCurrentLocation.observe(this, Observer {
            if (it == true) {
                requestLocationPermission()
            }
        })

        requestLocationPermission()

        return binding.root
    }

    //This shows the OptionsBottomSheet that lets you change location, date, and toggle online/offline fetching.
    private fun showOptionsBottomSheet() {
        OptionsBottomSheetFragment().show(
            requireActivity().supportFragmentManager,
            "options_fragment"
        )
    }


    private fun requestLocationPermission() {
        val shouldProvideRationale = shouldShowRequestPermissionRationale(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        //Provide an additional rationale to the user. This would happen if the user denied the request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            showUserDeniedPermissionsAlertDialog(false)
        } else { /*
            Request permission. It's possible this can be auto-answered if device policy sets the permission
            in a given state or the user denied the permission previously and checked "Never ask again".*/
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_LOCATION_ONLY_REQUEST_CODE
            )
        }
    }

    //This shows a dialog box that allows the user to either go to Setting and grant the Location permission or turn off the app's Location features.
    private fun showUserDeniedPermissionsAlertDialog(bUserCheckedDontShowAgainBox: Boolean) {
        alertDialog?.dismiss()

        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton(
            "Enable permission"
        ) { dialog, id ->
            // User wants to enable Location permission
            if (bUserCheckedDontShowAgainBox) {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri =
                    Uri.fromParts(
                        "package",
                        BuildConfig.APPLICATION_ID, null
                    )
                intent.data = uri
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_LOCATION_ONLY_REQUEST_CODE
                )
            }
        }
        builder.setNegativeButton("Choose location manually") { dialog, id ->
            //TODO: show optionsBottomSheet by triggering an event in HomeViewModel
            alertDialog?.dismiss()
        }
        builder.setTitle("App needs a location")
        builder.setMessage(
            "If you deny the Location permission, you'll have to choose a location manually. " +
                    "Otherwise, the app can't display any sunrise/sunset data!"
        )
        alertDialog = builder.create()
        alertDialog?.show()
    }


    //Callback received when a permissions request has been completed.
    override fun onRequestPermissionsResult (
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (permissions.isEmpty() || grantResults.isEmpty()) {
            return
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission was granted.
            val repository = SunRepository(requireActivity().application)
            repository.updateLocation(null)

        } else { // Permission was denied and user checked the "Don't ask again" checkbox.
            showUserDeniedPermissionsAlertDialog(true)
        }
    }

}
