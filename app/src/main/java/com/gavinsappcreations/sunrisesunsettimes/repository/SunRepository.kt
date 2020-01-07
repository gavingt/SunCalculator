package com.gavinsappcreations.sunrisesunsettimes.repository

import android.app.Application
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.gavinsappcreations.sunrisesunsettimes.calculations.SunriseSunsetCalculator
import com.gavinsappcreations.sunrisesunsettimes.utilities.SharedPreferenceBooleanLiveData
import com.gavinsappcreations.sunrisesunsettimes.utilities.SharedPreferenceStringLiveData
import com.google.android.gms.location.LocationServices
import java.util.*

const val SUNRISE_TIME_KEY = "rise_time"
const val SUNSET_TIME_KEY = "set_time"
const val LATITUDE_AND_LONGITUDE_KEY = "lat_long"
const val USING_CURRENT_LOCATION_KEY = "custom_location"
const val FETCHING_DATA_ONLINE_KEY = "fetch_online"


//This is a simplified repository where we use LiveSharedPreferences instead of a database
class SunRepository(application: Application) {

    //This is used any time we want to fetch a preference
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    val sunriseTime = SharedPreferenceStringLiveData(prefs, SUNRISE_TIME_KEY, "")
    val sunsetTime = SharedPreferenceStringLiveData(prefs, SUNSET_TIME_KEY, "")

    //This is the latitude and longitude of the location of interest, separated by a comma (no spaces)
    val latitudeAndLongitude = SharedPreferenceStringLiveData(prefs, LATITUDE_AND_LONGITUDE_KEY, "")

    //If we're fetching sun times at the current location, this will be true.
    //If user sets a custom location, this will be false.
    val usingCurrentLocation = SharedPreferenceBooleanLiveData(prefs, USING_CURRENT_LOCATION_KEY, true)

    //TODO: change defValue to true
    val fetchingDataOnline = SharedPreferenceBooleanLiveData(prefs, FETCHING_DATA_ONLINE_KEY, false)


    fun updateDate() {

    }


    //Handle storing a new city name.
    fun updateCity() {
        //TODO: use PlaceAutocomplete for finding just city names
    }


    //Handle what to do when a new location of interest is chosen,
    //either from fusedLocation or the user's custom location selection.
    fun updateLocation() {
        if (prefs.getBoolean(USING_CURRENT_LOCATION_KEY, true)) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                location ?: return@addOnSuccessListener
                updateCurrentLocationPref(location)
            }
        } else {
            //TODO: instead of this, get new location info from web
            val location = Location("")
            location.apply {latitude = 0.0; longitude = 0.0}
            updateCurrentLocationPref(location)
        }

        //Since location has changed, we also need to update sun data
        updateSunData()
    }


    //Update the SharedPreference value that stores that latitude and longitude of the location of interest.
    private fun updateCurrentLocationPref(newLocation: Location) {
        prefs.edit()
            .putString(
                LATITUDE_AND_LONGITUDE_KEY,
                "${newLocation.latitude},${newLocation.longitude}"
            )
            .apply()
    }



    private fun updateSunData() {
        val latLongString = prefs.getString(LATITUDE_AND_LONGITUDE_KEY, "")

        if (latLongString.isNullOrEmpty()) {
            return
        }

        val latLongArray = latLongString.split(",")
        val location = Location("").apply {
            latitude = latLongArray[0].toDouble(); longitude = latLongArray[1].toDouble()}

        val sunriseString: String
        val sunsetString: String

        //TODO: change default to true
        if (prefs.getBoolean(FETCHING_DATA_ONLINE_KEY, false)) {
            //TODO: handle online fetching of data
            sunriseString = "TODO: fetch sunrise online"
            sunsetString = "TODO: fetch sunset online"
        } else {
            val calculator = SunriseSunsetCalculator(location, TimeZone.getDefault())
            val calendar = Calendar.getInstance()
            sunriseString = "Sunrise: ${calculator.getOfficialSunriseForDate(calendar)}"
            sunsetString = "Sunset: ${calculator.getOfficialSunsetForDate(calendar)}"
        }

        prefs.edit().putString(
            SUNRISE_TIME_KEY,
            sunriseString
        )
            .putString(
                SUNSET_TIME_KEY,
                sunsetString
            ).apply()
    }





    fun updateFetchOnline() {

    }
}