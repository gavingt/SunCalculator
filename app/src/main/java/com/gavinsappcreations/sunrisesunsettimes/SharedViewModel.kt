package com.gavinsappcreations.sunrisesunsettimes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gavinsappcreations.sunrisesunsettimes.network.SunNetwork
import com.gavinsappcreations.sunrisesunsettimes.network.TimeZoneNetwork
import com.gavinsappcreations.sunrisesunsettimes.network.asDomainModel
import com.gavinsappcreations.sunrisesunsettimes.utilities.formatDateResultFromApi
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

    /**
     * We use this value to fill in the progress in the ProgressBar. The values starts at 0,
     * then is incremented to 1 and finally 2 to indicate the TimeZone data and sun data being fetched.
     */
    private val _loadingProgress = MutableLiveData<Int>()
    val loadingProgress : LiveData<Int>
        get() = _loadingProgress

    init {
        _loadingProgress.value = 0
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

            //Reset loading progress
            _loadingProgress.value = 0

            //Initially set timeZoneId to the one returned by the user's device.
            var timeZoneId = TimeZone.getDefault().id

            //If we're using a custom location, overwrite timeZoneId with the result from the TimeZone API.
            if (_usingCustomLocation.value == true) {
                /**
                 * TimeZoneNetwork.timeZone.getTimeZoneData() is already returning a deferred thanks
                 * to Retrofit, so we don't need to call it from an async block. We just need to
                 * await() its result.
                 */
                timeZoneId = TimeZoneNetwork.timeZone
                    .getTimeZoneData(
                        "${place.latLng!!.latitude},${place.latLng!!.longitude}",
                        calendar.timeInMillis.shr(3),
                        PLACES_API_KEY
                    )
                    .await().asDomainModel().timeZoneId
            }

            //Set loadingProgress value to 1 to indicate we've fetched the TimeZone.
            _loadingProgress.value = 1

            val timeZoneFromApi = TimeZone.getTimeZone(timeZoneId)

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val formattedDateForApi = formatter.format(calendar.time)

            val sunData = SunNetwork.sunData.getSunData(
                place.latLng!!.latitude,
                place.latLng!!.longitude,
                formattedDateForApi
            ).await().results.asDomainModel()

            //Set loadingProgress value to 2 to indicate we've fetched the sun data.
            _loadingProgress.value = 2

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

}
