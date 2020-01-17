package com.gavinsappcreations.sunrisesunsettimes.utilities

import android.animation.ObjectAnimator
import android.graphics.Typeface
import android.text.format.DateUtils
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.gavinsappcreations.sunrisesunsettimes.R
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

//Formats and sets the text for the selected date.
@BindingAdapter("dateText")
fun TextView.setDateText(timeInMillis: Long?) {
    if (timeInMillis == null || DateUtils.isToday(timeInMillis)
    ) {
        text = context.getString(R.string.today)
    } else {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        text = formatter.format(calendar.time)
    }
}


//Controls the visibility of sun data views while loading is occurring and if an error has occurred.
@BindingAdapter("loadingInProgress", "hideSunDataIfInErrorState")
fun View.setSunViewVisibility(loadingProgress: Int, inErrorState: Boolean?) {

    if (inErrorState == true) {
        visibility = View.GONE
        return
    }

    if (loadingProgress < 2) {
        visibility = View.GONE
    } else {
        CoroutineScope(Dispatchers.Main).launch {
            delay(LOADING_PROGRESS_ANIMATION_TIME)
            visibility = View.VISIBLE
        }
    }
}


//Sets visibility of views that should be shown only if a network error has occurred.
@BindingAdapter("showIfInErrorState")
fun View.showIfInErrorState(inErrorState: Boolean?) {
    visibility = if (inErrorState == true) {
        View.VISIBLE
    } else {
        View.GONE
    }
}


/**
 * Update the ProgressBar progress with a smooth animation. We multiply the loadingProgress
 * by 50 so that the ProgressBar actually has some values to animate over. This method also
 * handles setting the visibility of the ProgressBar based on multiple factors.
 */
@BindingAdapter("progressBarProgress", "hideProgressBarIfInErrorState", "fetchingSunData")
fun ProgressBar.updateProgressBarProgress(
    newProgress: Int,
    inErrorState: Boolean?,
    fetchingSunData: Boolean?
) {

    if (inErrorState == true) {
        visibility = View.GONE
        return
    }

    /**
     * If sun data is being fetched, always set to visible. It will only be hidden after
     * the final animation has ended.
     */
    if (fetchingSunData == true) {
        visibility = View.VISIBLE
    }

    val progressStart = (newProgress - 1) * 50
    val progressEnd = newProgress * 50

    progress = progressStart

    val animation = ObjectAnimator.ofInt(this, "progress", progressStart, progressEnd)
    animation.duration = LOADING_PROGRESS_ANIMATION_TIME
    animation.interpolator = DecelerateInterpolator()
    animation.start()
    //Wait until animation ends to hide ProgressBar.
    animation.doOnEnd {
        if (progressEnd == 100) {
            visibility = View.GONE
        }
    }
}


//Sets visibility of cityTextInputLayout based on the value of usingCustomLocation.
@BindingAdapter("showIfUsingCustomLocation")
fun TextInputLayout.showIfUsingCustomLocation(usingCustomLocation: Boolean?) {
    visibility = if (usingCustomLocation == true) {
        View.VISIBLE
    } else {
        View.GONE
    }
}


//Sets text in BottomSheet's cityTextInputEditText based on the value of place.name.
@BindingAdapter("bottomSheetCityText")
fun TextInputEditText.setBottomSheetCityText(place: Place?) {
    setText(
        if (place == null || place.name == context.getString(R.string.current_location)) {
            ""
        } else {
            place.name
        }
    )
}


//Sets text in HomeFragment's locationTextView based on the value of place.name.
@BindingAdapter("homeFragmentCityName", "locationPermissionGranted")
fun TextView.setHomeFragmentCityText(place: Place?, locationPermissionGranted: Boolean?) {
    if (place != null && place.name != context.getString(R.string.current_location)) {
        text = place.name
        setTextColor(ContextCompat.getColor(context, R.color.colorText))
        setTypeface(null, Typeface.NORMAL)
    } else {
        if (locationPermissionGranted == true) {
            text = context.getString(R.string.current_location)
            setTextColor(ContextCompat.getColor(context, R.color.colorText))
            setTypeface(null, Typeface.NORMAL)
        } else {
            text = context.getString(R.string.missing_permission)
            setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            setTypeface(null, Typeface.BOLD)
        }
    }
}
