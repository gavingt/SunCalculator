package com.gavinsappcreations.sunrisesunsettimes.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.gavinsappcreations.sunrisesunsettimes.network.SunNetwork
import com.gavinsappcreations.sunrisesunsettimes.network.TimeZoneNetwork
import com.gavinsappcreations.sunrisesunsettimes.network.asDomainModel
import com.gavinsappcreations.sunrisesunsettimes.utilities.PLACES_API_KEY
import com.gavinsappcreations.sunrisesunsettimes.utilities.formatDateResultFromApi
import com.gavinsappcreations.sunrisesunsettimes.utilities.isNetworkAvailable
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*


class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val _place = MutableLiveData<Place?>()
    val place: LiveData<Place?>
        get() = _place

    fun onPlaceChanged(place: Place?) {
        if (_place.value != place) {
            _place.value = place
            updateSunData()
        }
    }


    //This will be true if a network error occurred and the user has yet to retry the request.
    private val _inErrorState = MutableLiveData<Boolean?>()
    val inErrorState: LiveData<Boolean?>
        get() = _inErrorState



    //Event that determines when optionsBottomSheet should be shown.
    private val _triggerOptionsBottomSheetEvent = MutableLiveData<Boolean>()
    val triggerOptionsBottomSheetEvent: LiveData<Boolean>
        get() = _triggerOptionsBottomSheetEvent

    //This is called through data binding once optionsButton is pressed.
    fun showOptionsBottomSheet() {
        _triggerOptionsBottomSheetEvent.value = true
    }

    //This is called from HomeFragment after the optionsButton has been pressed.
    fun doneShowingOptionsBottomSheet() {
        _triggerOptionsBottomSheetEvent.value = false
    }


    /**
     * We use this value to fill in the progress in the ProgressBar. The values starts at 0,
     * then is incremented to 1 and finally 2 to indicate the TimeZone data and sun data being fetched.
     */
    private val _loadingProgress = MutableLiveData<Int>()
    val loadingProgress: LiveData<Int>
        get() = _loadingProgress

    init {
        _loadingProgress.value = 0
    }



    private val _locationPermissionGrantedState = MutableLiveData<Boolean?>()
    val locationPermissionGrantedState: LiveData<Boolean?>
        get() = _locationPermissionGrantedState

    fun onLocationPermissionGrantedStateChanged(locationPermissionGranted: Boolean?) {
        _locationPermissionGrantedState.value = locationPermissionGranted
    }

    private val _triggerRequestLocationPermissionEvent = MutableLiveData<Boolean>()
    val triggerRequestLocationPermissionEvent: LiveData<Boolean>
        get() = _triggerRequestLocationPermissionEvent

    //This is called from OptionsBottomSheetFragment when it needs to request the Location permission.
    fun requestLocationPermission() {
        _triggerRequestLocationPermissionEvent.value = true
    }

    //This is called from MainActivity after the Location permission is done being requested.
    fun doneRequestingLocationPermission() {
        _triggerOptionsBottomSheetEvent.value = false
    }



    /**
     * We need a second variable to signal to ProgressBar when loading is occurring, since
     * the loadingProgress is updated so rapidly in some circumstances that all its values aren't
     * necessarily broadcast to observers. This makes loadingProgress unreliable for the task of
     * telling if loading is actively occurring.
     */
    private val _fetchingSunData = MutableLiveData<Boolean?>()
    val fetchingSunData: LiveData<Boolean?>
        get() = _fetchingSunData



    private val _usingCustomLocation = MutableLiveData<Boolean>()
    val usingCustomLocation: LiveData<Boolean>
        get() = _usingCustomLocation

    init {
        _usingCustomLocation.value = false
    }

    fun onUsingCustomLocationChanged(usingCustomLocationNewValue: Boolean) {
        if (_usingCustomLocation.value != usingCustomLocationNewValue) {
            _usingCustomLocation.value = usingCustomLocationNewValue
        }
    }


    //The date the user selects to show sun data for.
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


    fun updateSunData() {

        if (isNetworkAvailable(getApplication())) {
            //Reset errorOccurred.value since we're starting/restarting the network requests.
            _inErrorState.value = null
        } else {
            _inErrorState.value = true
            return
        }

        //Set loadingProgress.value to 0 to signal to the ProgressBar to begin its animation.
        _loadingProgress.value = 0

        //Set fetchingSunData.value to true to signal to ProgressBar to become visible.
        _fetchingSunData.value = true

        val place = _place.value
        if (place == null || place.latLng == null || place.utcOffsetMinutes == null) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {

            val calendar = Calendar.getInstance()
            if (_dateInMillis.value != null) {
                calendar.timeInMillis = _dateInMillis.value!!
            }

            try {
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

                //Update loadingProgress.value to 1 to indicate the first API call is complete.
                _loadingProgress.postValue(1)

                val timeZoneFromApi = TimeZone.getTimeZone(timeZoneId)

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

                _sunriseTime.postValue(
                    "Sunrise: ${formatDateResultFromApi(sunriseApiDateString, timeZoneFromApi)}"
                )
                _sunsetTime.postValue(
                    "Sunset: ${formatDateResultFromApi(sunsetApiDateString, timeZoneFromApi)}"
                )

                //Update loadingProgress.value to 2 to indicate the second API call is complete.
                _loadingProgress.postValue(2)
                //Set fetchingSunData.value to null to indicate that fetching is complete.
                _fetchingSunData.postValue(null)

            } catch (e: Exception) {
                Log.d("LOG", "Error: ${e.message ?: "No message"}")
                //Set errorOccurred.value to true so the UI can display an error message.
                _inErrorState.postValue(true)
                //Set fetchingSunData.value to null to indicate that fetching is complete.
                _fetchingSunData.value = null
            }
        }
    }


    //Factory for constructing SharedViewModel with Application parameter.
    class Factory(val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SharedViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}
