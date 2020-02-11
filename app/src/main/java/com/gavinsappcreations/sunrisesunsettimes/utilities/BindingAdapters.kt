package com.gavinsappcreations.sunrisesunsettimes.utilities

import android.animation.ObjectAnimator
import android.graphics.Typeface
import android.text.format.DateUtils
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.gavinsappcreations.sunrisesunsettimes.R
import com.gavinsappcreations.sunrisesunsettimes.network.NetworkState
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Formats and sets the text for the selected date.
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


// Controls the visibility of sun data views while loading is occurring and if an error has occurred.
@BindingAdapter("sunDataVisibility")
fun View.setSunViewVisibility(networkState: NetworkState) {

    when (networkState) {
        NetworkState.NetworkFailure -> {
            visibility = View.GONE
            return
        }
        NetworkState.NetworkSuccess -> {
            CoroutineScope(Dispatchers.Main).launch {
                delay(LOADING_PROGRESS_ANIMATION_TIME)
                visibility = View.VISIBLE
            }
        }
        else -> { //networkState == NetworkState.NetworkLoading
            visibility = View.GONE
        }
    }
}


// Sets visibility of views that should be shown if network error or device's location is disabled.
@BindingAdapter("showIfError")
fun View.showIfError(networkState: NetworkState) {

    val networkErrorOccurred = networkState == NetworkState.NetworkFailure
    val locationDisabledErrorOccurred = networkState == NetworkState.LocationDisabled

    if (this.id == R.id.error_textView) {
        this as TextView

        if (locationDisabledErrorOccurred) {
            text = context.getString(R.string.enable_location_instructions)
        } else if (networkErrorOccurred) {
            text = context.getString(R.string.no_network_connection)
        }
    }

    visibility = if (networkErrorOccurred || locationDisabledErrorOccurred) {
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
@BindingAdapter("updateProgressAndVisibility")
fun ProgressBar.updateProgressBarProgress(
    networkState: NetworkState
) {

    val progressStart: Int
    val progressEnd: Int

    when (networkState) {
        NetworkState.AwaitingPermission, NetworkState.LocationDisabled -> {
            return
        }
        NetworkState.NetworkFailure, NetworkState.PermissionDenied -> {
            visibility = View.GONE
            return
        }
        NetworkState.NetworkSuccess -> {
            progressStart = 50
            progressEnd = 100
        }
        else -> { // networkState = NetworkState.NetworkLoading
            visibility = View.VISIBLE
            val networkLoading = networkState as NetworkState.NetworkLoading
            progressStart = (networkLoading.progress - 1) * 50
            progressEnd = networkLoading.progress * 50
        }
    }

    progress = progressStart

    val animation = ObjectAnimator.ofInt(this, "progress", progressStart, progressEnd)
    animation.duration = LOADING_PROGRESS_ANIMATION_TIME
    animation.interpolator = DecelerateInterpolator()
    animation.start()
    // Wait until animation ends to hide ProgressBar.
    animation.doOnEnd {
        if (progressEnd == 100) {
            visibility = View.GONE
        }
    }
}


// Sets visibility of cityTextInputLayout based on the value of usingCustomLocation.
@BindingAdapter("showIfUsingCustomLocation")
fun TextInputLayout.showIfUsingCustomLocation(usingCustomLocation: Boolean?) {
    visibility = if (usingCustomLocation == true) {
        View.VISIBLE
    } else {
        View.GONE
    }
}


// Sets text in BottomSheet's cityTextInputEditText based on the value of place.name.
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


// Sets text in HomeFragment's locationTextView based on the value of place.name.
@BindingAdapter("homeFragmentCityName", "networkState")
fun TextView.setHomeFragmentCityText(place: Place?, networkState: NetworkState) {

    if (place != null && place.name != context.getString(R.string.current_location)) {
        text = place.name
        @ColorInt
        val color = resolveColorAttr(context, android.R.attr.textColorPrimary)
        setTextColor(color)
        setTypeface(null, Typeface.NORMAL)
    } else {
        when (networkState) {
            NetworkState.AwaitingPermission -> {
                text = ""
            }
            NetworkState.PermissionDenied -> {
                text = context.getString(R.string.missing_permission)
                setTextColor(ContextCompat.getColor(context, R.color.colorErrorText))
                setTypeface(null, Typeface.BOLD)
            }
            NetworkState.LocationDisabled -> {
                text = context.getString(R.string.location_disabled)
                setTextColor(ContextCompat.getColor(context, R.color.colorErrorText))
                setTypeface(null, Typeface.BOLD)
            }
            else -> {
                text = context.getString(R.string.current_location)
                @ColorInt
                val color = resolveColorAttr(context, android.R.attr.textColorPrimary)
                setTextColor(color)
                setTypeface(null, Typeface.NORMAL)
            }
        }
    }

}