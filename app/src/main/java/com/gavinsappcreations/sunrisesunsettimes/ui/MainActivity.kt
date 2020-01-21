package com.gavinsappcreations.sunrisesunsettimes.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gavinsappcreations.sunrisesunsettimes.BuildConfig
import com.gavinsappcreations.sunrisesunsettimes.R
import com.gavinsappcreations.sunrisesunsettimes.utilities.MILLISECONDS_PER_MINUTE
import com.gavinsappcreations.sunrisesunsettimes.utilities.REQUEST_PERMISSIONS_LOCATION_ONLY_REQUEST_CODE
import com.gavinsappcreations.sunrisesunsettimes.viewmodels.SharedViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import java.util.*

class MainActivity : AppCompatActivity() {

    // Create sharedViewModel as an AndroidViewModel, passing in Application to the Factory.
    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProviders.of(this, SharedViewModel.Factory(application))
            .get(SharedViewModel::class.java)
    }

    private var alertDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = getString(R.string.app_name)

        // If place is null, we fetch the user's location.
        sharedViewModel.place.observe(this, Observer {
            if (it == null) {
                requestLocationPermission()
            }
        })

        /**
         * Only request location permission if we're not using a custom location. Otherwise, if the
         * user denied the permission, they'd be prompted with the permission dialog on every
         * configuration change.
         */
        if (sharedViewModel.usingCustomLocation.value != true) {
            requestLocationPermission()
        }

        sharedViewModel.triggerRequestLocationPermissionEvent.observe(this, Observer {
            if (it == true) {
                requestLocationPermission()
                sharedViewModel.doneRequestingLocationPermission()
            }
        })
    }

    private fun requestLocationPermission() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        //Provide an additional rationale to the user. This would happen if the user denied the request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            showUserDeniedPermissionsAlertDialog(false)
        } else {
            /*
            Request permission. It's possible this can be auto-answered if device policy sets the permission
            in a given state or the user denied the permission previously and checked "Never ask again".
            */
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_LOCATION_ONLY_REQUEST_CODE
            )
        }
    }

    //This shows a dialog box that allows the user to either go to Setting and grant the Location permission or turn off the app's Location features.
    private fun showUserDeniedPermissionsAlertDialog(bUserCheckedDontShowAgainBox: Boolean) {
        alertDialog?.dismiss()

        val builder = AlertDialog.Builder(this)
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
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_LOCATION_ONLY_REQUEST_CODE
                )
            }
        }
        builder.setNegativeButton(getString(R.string.use_custom_location)) { _, _ ->
            sharedViewModel.onLocationPermissionGrantedStateChanged(false)
            sharedViewModel.onUsingCustomLocationChanged(usingCustomLocationNewValue = true)
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
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (permissions.isEmpty() || grantResults.isEmpty()) {
            return
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            /**
             * If locationPermissionGranted.value != true and we've made it here, this means
             * we've just granted the permission from the BottomSheet. So to set the RadioButton
             * back to "Using current location", we need to change usingCustomLocation.value here.
             * Also, to set a new place.value, we need to set the existing place.value to null,
             * since it could possibly be a custom location (and therefore the onPlaceChanged()
             * method further below wouldn't get called.
             */
            if (sharedViewModel.locationPermissionGrantedState.value != true) {
                sharedViewModel.onUsingCustomLocationChanged(false)
                sharedViewModel.onPlaceChanged(null)
            }

            sharedViewModel.onLocationPermissionGrantedStateChanged(true)

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                location ?: return@addOnSuccessListener
                val placeBuilder = Place.builder()
                placeBuilder.setLatLng(LatLng(location.latitude, location.longitude))
                placeBuilder.setName(getString(R.string.current_location))

                val tz = TimeZone.getDefault()
                val now = Date()
                val utcOffsetMinutes = tz.getOffset(now.time) / MILLISECONDS_PER_MINUTE
                placeBuilder.setUtcOffsetMinutes(utcOffsetMinutes)

                /**
                 * Only update sharedViewModel.place based on current location if we haven't
                 * done it yet. Otherwise we would be updating the place unconditionally every
                 * time a configuration change occurs on the device.
                 */
                if (sharedViewModel.place.value == null) {
                    sharedViewModel.onPlaceChanged(placeBuilder.build())
                }


            }

        } else { // Permission was denied.
            sharedViewModel.onLocationPermissionGrantedStateChanged(false)
            sharedViewModel.onUsingCustomLocationChanged(usingCustomLocationNewValue = true)
            showUserDeniedPermissionsAlertDialog(true)
        }
    }

}
