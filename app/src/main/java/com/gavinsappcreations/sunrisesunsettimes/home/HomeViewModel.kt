package com.gavinsappcreations.sunrisesunsettimes.home

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.gavinsappcreations.sunrisesunsettimes.repository.SunRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SunRepository(application)

    val location: LiveData<Location> = Transformations.map(repository.latitudeAndLongitude) {
        //This turns our SharedPreferenceStringLiveData from the repository into a LiveData<Location>
        if (it.isNotEmpty()) {
            val locationArray = it.split(",")
            val newLocation = Location("").apply {
                latitude = locationArray[0].toDouble()
                longitude = locationArray[1].toDouble()
            }
            newLocation
        } else {
            null
        }
    }


    val sunriseTime = repository.sunriseTime
    val sunsetTime = repository.sunsetTime


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
