package com.gavinsappcreations.sunrisesunsettimes.options

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.gavinsappcreations.sunrisesunsettimes.R
import com.gavinsappcreations.sunrisesunsettimes.repository.KEY_FETCHING_DATA_ONLINE
import com.gavinsappcreations.sunrisesunsettimes.repository.KEY_USING_CURRENT_LOCATION
import com.gavinsappcreations.sunrisesunsettimes.repository.SunRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import kotlinx.android.synthetic.main.options_bottom_sheet_layout.view.*

class OptionsViewModel (application: Application) : AndroidViewModel(application) {

    private val repository = SunRepository(application)

    val usingCurrentLocation = repository.usingCurrentLocation

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


    fun updateUsingCurrentLocation (usingCurrentLocation: Boolean) {
        repository.updateUsingCurrentLocation(usingCurrentLocation)
    }



    fun updateLocation(place: Place) {
        repository.updateLocation(place)
    }


}