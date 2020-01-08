package com.gavinsappcreations.sunrisesunsettimes.repository

import android.app.Application
import android.content.SharedPreferences
import android.location.Location
import androidx.preference.PreferenceManager
import com.gavinsappcreations.sunrisesunsettimes.R
import com.gavinsappcreations.sunrisesunsettimes.calculations.SunriseSunsetCalculator
import com.gavinsappcreations.sunrisesunsettimes.utilities.SharedPreferenceBooleanLiveData
import com.gavinsappcreations.sunrisesunsettimes.utilities.SharedPreferenceStringLiveData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import java.util.*


const val KEY_SUNRISE_TIME = "rise_time"
const val KEY_SUNSET_TIME = "set_time"
const val KEY_PLACE = "place"
const val KEY_USING_CURRENT_LOCATION = "current_location"
const val KEY_FETCHING_DATA_ONLINE = "fetch_online"


//In this repository we use LiveSharedPreferences instead of a database
class SunRepository(val application: Application) {

    //This is used any time we want to fetch a preference
    private val prefs: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(application)

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    val sunriseTime = SharedPreferenceStringLiveData(prefs, KEY_SUNRISE_TIME, "")
    val sunsetTime = SharedPreferenceStringLiveData(prefs, KEY_SUNSET_TIME, "")

    /*
    *  This is the latitude, longitude, city name, and UTC offset of the place of interest,
    *  separated by commas (with no spaces)
    */
    val place = SharedPreferenceStringLiveData(prefs, KEY_PLACE, "")


    //If we're fetching sun times at the current location, this will be true.
    //If user sets a custom location, this will be false.
    val usingCurrentLocation =
        SharedPreferenceBooleanLiveData(prefs, KEY_USING_CURRENT_LOCATION, true)

    //TODO: change defValue to true
    val fetchingDataOnline = SharedPreferenceBooleanLiveData(prefs, KEY_FETCHING_DATA_ONLINE, false)


    fun updateDate() {

    }


    fun updateUsingCurrentLocation(usingCurrentLocation: Boolean) {
        prefs.edit().putBoolean(KEY_USING_CURRENT_LOCATION, usingCurrentLocation).apply()
    }


    //Handle what to do when a new location of interest is chosen,
    //either from fusedLocation or the user's custom location selection.
    fun updateLocation(place: Place?) {
        if (usingCurrentLocation.getValueFromPreferences(KEY_USING_CURRENT_LOCATION, true)) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                location ?: return@addOnSuccessListener
                val placeBuilder = Place.builder()
                placeBuilder.setLatLng(LatLng(location.latitude, location.longitude))
                placeBuilder.setName(application.getString(R.string.current_location))

                //TODO: get UTC offset in minutes of current location
                val tz = TimeZone.getDefault()
                val now = Date()
                val utcOffsetMinutes = tz.getOffset(now.time) / 60000
                placeBuilder.setUtcOffsetMinutes(utcOffsetMinutes)

                updateCurrentPlacePref(placeBuilder.build())

                //Since location has changed, we also need to update sun data
                updateSunData()
            }
        } else {
            updateCurrentPlacePref(place)
            //Since location has changed, we also need to update sun data
            updateSunData()
        }
    }


    //Update the SharedPreference value that stores data for the current place of interest
    private fun updateCurrentPlacePref(newPlace: Place?) {
        newPlace?.let {
            prefs.edit()
                .putString(
                    KEY_PLACE,
                    "${newPlace.latLng!!.latitude},${newPlace.latLng!!.longitude},${newPlace.name},${newPlace.utcOffsetMinutes}"
                )
                .apply()
        }
    }


    private fun updateSunData() {
        val placeString = prefs.getString(KEY_PLACE, "")

        if (placeString.isNullOrEmpty()) {
            return
        }

        val placeArray = placeString.split(",")
        val location = Location("").apply {
            latitude = placeArray[0].toDouble(); longitude = placeArray[1].toDouble()
        }
        val utcOffsetMinutes: Int = placeArray[3].toInt()

        val sunriseString: String
        val sunsetString: String

        //TODO: change default to true
        if (prefs.getBoolean(KEY_FETCHING_DATA_ONLINE, false)) {
            //TODO: handle online fetching of data
            sunriseString = "TODO: fetch sunrise online"
            sunsetString = "TODO: fetch sunset online"
        } else {
            val calculator = SunriseSunsetCalculator(location, TimeZone.getDefault())
            val calendar = Calendar.getInstance()
            sunriseString = "Sunrise: ${calculator.getOfficialSunriseForDateAndUtcOffset(calendar, utcOffsetMinutes)}"
            sunsetString = "Sunset: ${calculator.getOfficialSunsetForDateAndUtcOffset(calendar, utcOffsetMinutes)}"
        }

        prefs.edit().putString(
            KEY_SUNRISE_TIME,
            sunriseString
        )
            .putString(
                KEY_SUNSET_TIME,
                sunsetString
            ).apply()
    }
}