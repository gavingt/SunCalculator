package com.gavinsappcreations.sunrisesunsettimes.home

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gavinsappcreations.sunrisesunsettimes.calculations.SunriseSunsetCalculator
import java.util.*

class HomeViewModel : ViewModel() {

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location>
        get() = _location

    fun setCurrentLocation(location: Location) {
        _location.value = location
    }


    private val _sunriseTime = MutableLiveData<String>()
    val sunriseTime: LiveData<String>
        get() = _sunriseTime

    private val _sunsetTime = MutableLiveData<String>()
    val sunsetTime: LiveData<String>
        get() = _sunsetTime

    fun calculateSunriseSunsetTimes(location: Location) {
        val calculator = SunriseSunsetCalculator(location, TimeZone.getDefault())
        val calendar = Calendar.getInstance()
        _sunriseTime.value = "Sunrise: ${calculator.getOfficialSunriseForDate(calendar)}"
        _sunsetTime.value = "Sunset: ${calculator.getOfficialSunsetForDate(calendar)}"
    }

    //Request the Options AlertDialog to be shown
    private val _showOptionsAlertDialogEvent = MutableLiveData<Boolean>()
    val showOptionsAlertDialogEvent: LiveData<Boolean>
        get() = _showOptionsAlertDialogEvent

    fun onOptionsButtonPressed() {
        _showOptionsAlertDialogEvent.value = true
    }

    fun doneShowingOptionsAlertDialog() {
        _showOptionsAlertDialogEvent.value = false
    }


    private val _city = MutableLiveData<String>()
    val city: LiveData<String>
        get() = _city

    fun setCity(city: String) {
        _city.value = city
    }

}
