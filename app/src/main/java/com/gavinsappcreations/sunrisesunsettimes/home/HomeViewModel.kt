package com.gavinsappcreations.sunrisesunsettimes.home

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.gavinsappcreations.sunrisesunsettimes.repository.SunRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SunRepository(application)

    val place: LiveData<Place> = Transformations.map(repository.place) {
        //This turns our SharedPreferenceStringLiveData from the repository into a LiveData<Place>
        if (it.isNotEmpty()) {
            val placeArray = it.split(",")
            val placeBuilder = Place.builder()
            placeBuilder.setLatLng(LatLng(placeArray[0].toDouble(), placeArray[1].toDouble()))
            placeBuilder.setName(placeArray[2])
            placeBuilder.setUtcOffsetMinutes(placeArray[3].toInt())
            placeBuilder.build()
        } else {
            null
        }
    }

    val usingCurrentLocation = repository.usingCurrentLocation


    val sunriseTime = repository.sunriseTime
    val sunsetTime = repository.sunsetTime



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


}
