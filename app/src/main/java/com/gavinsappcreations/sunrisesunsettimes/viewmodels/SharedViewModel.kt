package com.gavinsappcreations.sunrisesunsettimes.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.gavinsappcreations.sunrisesunsettimes.network.NetworkState
import com.gavinsappcreations.sunrisesunsettimes.network.SunNetwork
import com.gavinsappcreations.sunrisesunsettimes.network.TimeZoneNetwork
import com.gavinsappcreations.sunrisesunsettimes.network.asDomainModel
import com.gavinsappcreations.sunrisesunsettimes.utilities.GOOGLE_PLACES_AND_TIMEZONE_API_KEY
import com.gavinsappcreations.sunrisesunsettimes.utilities.formatDateResultFromApi
import com.gavinsappcreations.sunrisesunsettimes.utilities.isNetworkAvailable
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val _place = MutableLiveData<Place?>()
    val place: LiveData<Place?>
        get() = _place


    private val _networkState = MutableLiveData<NetworkState>()
    val networkState: LiveData<NetworkState>
        get() = _networkState

    init {
        _networkState.value = NetworkState.AwaitingPermission
    }


    // Event that determines when optionsBottomSheet should be shown.
    private val _triggerOptionsBottomSheetEvent = MutableLiveData<Boolean>()
    val triggerOptionsBottomSheetEvent: LiveData<Boolean>
        get() = _triggerOptionsBottomSheetEvent

    private val _triggerRequestLocationPermissionEvent = MutableLiveData<Boolean>()
    val triggerRequestLocationPermissionEvent: LiveData<Boolean>
        get() = _triggerRequestLocationPermissionEvent


    private val _usingCustomLocation = MutableLiveData<Boolean>()
    val usingCustomLocation: LiveData<Boolean>
        get() = _usingCustomLocation

    init {
        _usingCustomLocation.value = false
    }

    // The date the user selects to show sun data for.
    private val _dateInMillis = MutableLiveData<Long?>()
    val dateInMillis: LiveData<Long?>
        get() = _dateInMillis

    private val _sunriseTime = MutableLiveData<String>()
    val sunriseTime: LiveData<String>
        get() = _sunriseTime

    private val _sunsetTime = MutableLiveData<String>()
    val sunsetTime: LiveData<String>
        get() = _sunsetTime


    fun onPlaceChanged(newPlace: Place?) {
        if (_place.value != newPlace) {
            _place.value = newPlace
            updateSunData()
        }
    }

    fun onNetworkStateChanged(newNetworkState: NetworkState) {
        if (_networkState.value != newNetworkState) {
            _networkState.value = newNetworkState
        }
    }


    // This is called through data binding once optionsButton is pressed.
    fun showOptionsBottomSheet() {
        _triggerOptionsBottomSheetEvent.value = true
    }

    // This is called from HomeFragment after the optionsButton has been pressed.
    fun doneShowingOptionsBottomSheet() {
        _triggerOptionsBottomSheetEvent.value = false
    }


    // This is called from OptionsBottomSheetFragment when it needs to request the Location permission.
    fun requestLocationPermission() {
        _triggerRequestLocationPermissionEvent.value = true
    }

    // This is called from MainActivity after the Location permission is done being requested.
    fun doneRequestingLocationPermission() {
        _triggerOptionsBottomSheetEvent.value = false
    }

    fun onUsingCustomLocationChanged(usingCustomLocationNewValue: Boolean) {
        if (_usingCustomLocation.value != usingCustomLocationNewValue) {
            _usingCustomLocation.value = usingCustomLocationNewValue
        }
    }

    fun onDateChanged(newDateInMillis: Long?) {
        _dateInMillis.value = newDateInMillis
        updateSunData()
    }


    fun updateSunData() {

        val place = _place.value
        if (place == null || place.latLng == null || place.utcOffsetMinutes == null) {
            requestLocationPermission()
            return
        }

        if (isNetworkAvailable(getApplication())) {
            // Set loading progress to 0 to signal to the ProgressBar to begin its animation.
            _networkState.value = NetworkState.NetworkLoading(0)
        } else {
            _networkState.value = NetworkState.NetworkFailure
            return
        }

        viewModelScope.launch {

            val calendar = Calendar.getInstance()
            if (_dateInMillis.value != null) {
                calendar.timeInMillis = _dateInMillis.value!!
            }

            try {
                // Initially set timeZoneId to the one returned by the user's device.
                var timeZoneId = TimeZone.getDefault().id

                // If we're using a custom location, overwrite timeZoneId with the result from the TimeZone API.
                if (_usingCustomLocation.value == true) {
                    // Retrofit handles the await() call for us.
                    timeZoneId = TimeZoneNetwork.timeZone
                        .getTimeZoneData(
                            "${place.latLng!!.latitude},${place.latLng!!.longitude}",
                            calendar.timeInMillis.shr(3),
                            GOOGLE_PLACES_AND_TIMEZONE_API_KEY
                        )
                        .body()!!.asDomainModel().timeZoneId
                }

                // Update loading progress to 1 to indicate the first API call is complete.
                _networkState.postValue(NetworkState.NetworkLoading(1))

                val timeZoneFromApi = TimeZone.getTimeZone(timeZoneId)

                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val formattedDateForApi = formatter.format(calendar.time)

                // Retrofit handles the await() call for us.
                val sunData = SunNetwork.sunData.getSunData(
                    place.latLng!!.latitude,
                    place.latLng!!.longitude,
                    formattedDateForApi
                ).body()!!.results.asDomainModel()


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

                // Set networkState to NetworkSuccess to indicate that fetching is complete.
                _networkState.postValue(NetworkState.NetworkSuccess)

            } catch (e: Exception) {
                Log.d("LOG", "Error: ${e.message ?: "No message"}")
                // Set networkState to NetworkFailure to indicate that an error occurred.
                _networkState.postValue(NetworkState.NetworkFailure)
            }
        }
    }


    //Factory for constructing SharedViewModel with Application parameter.
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SharedViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}
