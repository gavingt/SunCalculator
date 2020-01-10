package com.gavinsappcreations.sunrisesunsettimes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gavinsappcreations.sunrisesunsettimes.calculations.SunriseSunsetCalculator
import com.gavinsappcreations.sunrisesunsettimes.network.TimeZoneNetwork
import com.gavinsappcreations.sunrisesunsettimes.network.asDomainModel
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.*
import java.util.*

class SharedViewModel : ViewModel() {

    private val _place = MutableLiveData<Place?>()
    val place: LiveData<Place?>
        get() = _place

    fun onPlaceChanged(place: Place?) {
        _place.value = place
        updateSunData()
    }

    private val _usingCustomLocation = MutableLiveData<Boolean>()
    val usingCustomLocation: LiveData<Boolean>
        get() = _usingCustomLocation


    fun onUsingCustomLocationChanged(usingCustomLocation: Boolean) {
        _usingCustomLocation.value = usingCustomLocation
    }


    //Request the OptionsBottomSheet to be shown
    private val _showOptionsBottomSheetEvent = MutableLiveData<Boolean>()
    val showOptionsBottomSheetEvent: LiveData<Boolean>
        get() = _showOptionsBottomSheetEvent

    fun onOptionsButtonPressed() {
        _showOptionsBottomSheetEvent.value = true
    }

    fun doneShowingOptionsBottomSheet() {
        _showOptionsBottomSheetEvent.value = false
    }


    private val _dateInMillis = MutableLiveData<Long?>()
    val dateInMillis: LiveData<Long?>
        get() = _dateInMillis

    fun onDateChanged(newDateInMillis: Long?) {
        _dateInMillis.value = newDateInMillis
        updateSunData()
    }


    private val _sunriseTime = MutableLiveData<String>()
    val sunriseTime: LiveData<String>
        get() = _sunriseTime

    private val _sunsetTime = MutableLiveData<String>()
    val sunsetTime: LiveData<String>
        get() = _sunsetTime


/*    private fun updateSunData() {
        val place = _place.value
        if (place == null || place.latLng == null || place.utcOffsetMinutes == null) {
            return
        }

        viewModelScope.launch {
            lateinit var timeZone: TimeZone

            */
    /**
     * If using custom location, fetch time zone from Google's Time Zone Api.
     * But if using current location, we can retrieve the time zone locally from the device.
     *//*
            if (_usingCustomLocation.value == true) {
                withContext(Dispatchers.IO) {
                    val networkTimeZoneData = TimeZoneNetwork.timeZone.getTimeZoneData().await()
                    val timeZoneData = networkTimeZoneData.asDomainModel()
                    timeZone = TimeZone.getTimeZone(timeZoneData.timeZoneId)
                }
            } else {
                timeZone = TimeZone.getDefault()
            }

            viewModelScope.launch {
                //TODO: handle online fetching of data here
            }

            val calculator = SunriseSunsetCalculator(place, timeZone)
            val calendar = Calendar.getInstance()
            val dateInMillis = _dateInMillis.value
            if (dateInMillis != null) {
                calendar.timeInMillis = dateInMillis
            }
            _sunriseTime.value = "Sunrise: ${calculator.getOfficialSunriseForDateAndUtcOffset(
                calendar,
                place.utcOffsetMinutes!!
            )}"
            _sunsetTime.value = "Sunset: ${calculator.getOfficialSunsetForDateAndUtcOffset(
                calendar,
                place.utcOffsetMinutes!!
            )}"
        }
    }*/




    private fun updateSunData() {
        val place = _place.value
        if (place == null || place.latLng == null || place.utcOffsetMinutes == null) {
            return
        }

        viewModelScope.launch {

            val calendar = Calendar.getInstance()
            if (_dateInMillis.value != null) {
                calendar.timeInMillis = _dateInMillis.value!!
            }

            /**
             * TimeZoneNetwork.timeZone.getTimeZoneData() is already returning a deferred thanks
             * to Retrofit, so we don't need to call it from an async block. We just need to
             * await() its result.
             */
            val timeZoneData = TimeZoneNetwork.timeZone
                .getTimeZoneData(
                    "${place.latLng!!.latitude},${place.latLng!!.longitude}",
                    calendar.timeInMillis.shr(3),
                    PLACES_API_KEY
                )
                .await().asDomainModel()
            val timeZoneFromApi = TimeZone.getTimeZone(timeZoneData.timeZoneId)


/*            viewModelScope.async {
                //TODO: handle online fetching of sun data here
            }*/

            val calculator = SunriseSunsetCalculator(place, TimeZone.getDefault())

            _sunriseTime.value = "Sunrise: ${calculator.getOfficialSunriseForDateAndUtcOffset(
                calendar,
                place.utcOffsetMinutes!!
            )}"
            _sunsetTime.value = "Sunset: ${calculator.getOfficialSunsetForDateAndUtcOffset(
                calendar,
                place.utcOffsetMinutes!!
            )}"
        }
    }

}
