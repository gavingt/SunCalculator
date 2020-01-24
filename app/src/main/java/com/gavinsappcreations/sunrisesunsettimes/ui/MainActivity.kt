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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gavinsappcreations.sunrisesunsettimes.BuildConfig
import com.gavinsappcreations.sunrisesunsettimes.R
import com.gavinsappcreations.sunrisesunsettimes.databinding.ActivityMainBinding
import com.gavinsappcreations.sunrisesunsettimes.network.NetworkState
import com.gavinsappcreations.sunrisesunsettimes.utilities.MILLISECONDS_PER_MINUTE
import com.gavinsappcreations.sunrisesunsettimes.utilities.REQUEST_PERMISSIONS_LOCATION_ONLY_REQUEST_CODE
import com.gavinsappcreations.sunrisesunsettimes.utilities.isLocationEnabled
import com.gavinsappcreations.sunrisesunsettimes.viewmodels.SharedViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    // Create sharedViewModel as an AndroidViewModel, passing in Application to the Factory.
    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProviders.of(this, SharedViewModel.Factory(application))
            .get(SharedViewModel::class.java)
    }

    private var alertDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.toolbar.title = getString(R.string.app_name)

        // If place is null, we fetch the user's location.
        sharedViewModel.place.observe(this, Observer {
            if (it == null) {
                requestLocationFromFusedLocationProvider()
            }
        })

        /**
         * Only request location permission if we're not using a custom location. Otherwise, if the
         * user denied the permission, they'd be prompted with the permission dialog on every
         * configuration change.
         */
        if (sharedViewModel.usingCustomLocation.value != true) {
            requestLocationFromFusedLocationProvider()
        }

        sharedViewModel.triggerRequestLocationPermissionEvent.observe(this, Observer {
            if (it == true) {
                requestLocationFromFusedLocationProvider()
                sharedViewModel.doneRequestingLocationPermission()
            }
        })
    }

    private fun requestLocationFromFusedLocationProvider() {
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
        ) { _, _ ->
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
            sharedViewModel.onNetworkStateChanged(NetworkState.PermissionDenied)
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
             * If networkState.value == NetworkState.AwaitingPermission, this means either
             * we've just started the app or we've just granted the permission from the BottomSheet.
             * So to set the RadioButton back to "Using current location", we need to change
             * usingCustomLocation.value here. Also, to set a new place.value, we need to set the
             * existing place.value to null, since it could possibly be a custom location
             * (and therefore the onPlaceChanged() method further below wouldn't get called.
             */
            if (sharedViewModel.networkState.value == NetworkState.AwaitingPermission) {
                sharedViewModel.onUsingCustomLocationChanged(false)
                sharedViewModel.onPlaceChanged(null)
            }


            /**
             * If using current location and we already have the current location, don't try to
             * re-fetch current location. This fixes a problem that was caused when the user
             * disabled Location on their device and then toggled dark theme, which would cause
             * the error Views to appear on top of the sun data views.
             */
            if (sharedViewModel.usingCustomLocation.value != true && sharedViewModel.place.value != null) {
                return
            }

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location == null) {
                    if (isLocationEnabled(application)) {
                        /**
                         * If location is null and the device has location enabled, we can for the
                         * device to fetch a location by creating our own LocationRequest.
                         */
                        forceFetchLocation()
                    } else {
                        /**
                         * If location is null and the device has location disabled, we change
                         * the network state to NetworkState.LocationDisabled so we can alert
                         * the user.
                         */
                        sharedViewModel.onNetworkStateChanged(NetworkState.LocationDisabled)
                    }
                    return@addOnSuccessListener
                }

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
            sharedViewModel.onNetworkStateChanged(NetworkState.PermissionDenied)
            sharedViewModel.onUsingCustomLocationChanged(usingCustomLocationNewValue = true)
            showUserDeniedPermissionsAlertDialog(true)
        }
    }


    /**
     * If user recently toggled their Location setting off/on on their device, FusedLocationProvider
     * will return a null location. This method forces the device to find a location by forming a
     * LocationRequest and repeating until it gets a non-null Location result.
     */
    private fun forceFetchLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 2500
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val mostRecentLocation = locationResult.lastLocation
                if (mostRecentLocation != null) {
                    // We found a location, so we can now stop further location updates.
                    LocationServices.getFusedLocationProviderClient(this@MainActivity)
                        .removeLocationUpdates(this)

                    // Now that the device has a location, request it from FusedLocationProvider.
                    requestLocationFromFusedLocationProvider()
                }
            }
        }
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, locationCallback, null)
    }

}
