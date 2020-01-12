package com.gavinsappcreations.sunrisesunsettimes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gavinsappcreations.sunrisesunsettimes.network.SunNetwork
import com.gavinsappcreations.sunrisesunsettimes.network.TimeZoneNetwork
import com.gavinsappcreations.sunrisesunsettimes.network.asDomainModel
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class SharedViewModel : ViewModel() {

    private val _place = MutableLiveData<Place?>()
    val place: LiveData<Place?>
        get() = _place

    fun onPlaceChanged(place: Place?) {
        if (_place.value != place) {
            _place.value = place
            updateSunData()
        }
    }

    private val _usingCustomLocation = MutableLiveData<Boolean>()
    val usingCustomLocation: LiveData<Boolean>
        get() = _usingCustomLocation

    init {
        _usingCustomLocation.value = false
    }


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

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val formattedDateForApi = formatter.format(calendar.time)

            val sunData = SunNetwork.sunData.getSunData(
                place.latLng!!.latitude,
                place.latLng!!.longitude,
                formattedDateForApi
            ).await().results.asDomainModel()

            /**
             * The API result for sunset/sunrise times doesn't include the date, so we
             * concatenate the date we used in the request earlier.
             */
            val sunriseApiDateString = formattedDateForApi + " " + sunData.sunrise
            val sunsetApiDateString = formattedDateForApi + " " + sunData.sunset

            _sunriseTime.value =
                "Sunrise: ${formatDateResultFromApi(sunriseApiDateString, timeZoneFromApi)}"
            _sunsetTime.value =
                "Sunset: ${formatDateResultFromApi(sunsetApiDateString, timeZoneFromApi)}"
        }
    }


    /**
     * The date string returned by api.sunrise-sunset.org is in UTC, so we convert it to
     * the correct time zone and format it the way we want it.
     */
    private fun formatDateResultFromApi(apiDateString: String, timeZone: TimeZone): String {

        /**
         * Parse the date string returned by the API into a Date object.
         */
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa", Locale.ENGLISH)
        val apiDate = simpleDateFormat.parse(apiDateString)

        /**
         * Use a calendar to turn the UTC time returned by the API into the current time for the
         * location of interest. Do this by adding timeZone.getOffset to apiDate
         */
        val calendar = Calendar.getInstance()
        calendar.time = apiDate!!
        calendar.add(Calendar.MILLISECOND, timeZone.getOffset(calendar.timeInMillis))
        val correctedDate = calendar.time

        /**
         * Apply the time pattern we want to show in our app
         */
        simpleDateFormat.applyPattern("hh:mm aa")
        return simpleDateFormat.format(correctedDate)
    }

}
