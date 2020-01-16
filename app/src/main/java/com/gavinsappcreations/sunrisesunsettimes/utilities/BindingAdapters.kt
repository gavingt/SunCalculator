package com.gavinsappcreations.sunrisesunsettimes.utilities

import android.animation.ObjectAnimator
import android.app.ProgressDialog.show
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.core.animation.doOnEnd
import com.gavinsappcreations.sunrisesunsettimes.R
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.*


//Sets the background and enables/disables the various parts of the autocomplete Fragment
@BindingAdapter("autocompleteBackground")
fun FrameLayout.setAutoCompleteBackground(usingCustomLocation: Boolean) {
    val editText = findViewById<EditText>(R.id.places_autocomplete_search_input)
    val imageView = findViewById<ImageView>(R.id.places_autocomplete_search_button)
    val button = findViewById<ImageButton>(R.id.places_autocomplete_clear_button)

    if (usingCustomLocation) {
        background = ContextCompat.getDrawable(context, R.drawable.rounded_rectangle_enabled)
        editText.isEnabled = true
        imageView.isEnabled = true
        button.isEnabled = true
    } else {
        background = ContextCompat.getDrawable(context, R.drawable.rounded_rectangle_disabled)
        editText.isEnabled = false
        imageView.isEnabled = false
        button.isEnabled = false
    }
}


@BindingAdapter("setDateText")
fun TextView.setDateText(timeInMillis: Long?) {
    if (timeInMillis == null || DateUtils.isToday(timeInMillis)
    ) {
        text = context.getString(R.string.today)
    } else {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        val formatter = SimpleDateFormat("MMM dd, yyyy")
        text = formatter.format(calendar.time)
    }
}


//Controls the visibility of views in HomeFragment while loading is occurring.
@BindingAdapter("loadingInProgress", "datePickerSettling")
fun View.setSunViewVisibility(loadingInProgress: Boolean, datePickerSettling: Boolean) {
    visibility = if (datePickerSettling || loadingInProgress) {
        View.INVISIBLE
    } else {
        View.VISIBLE
    }
}


//Controls the visibility of the ProgressBar and its accompanying TextView.
@BindingAdapter("setProgressBarVisibility")
fun ContentLoadingProgressBar.setProgressBarVisibility(loadingInProgress: Boolean) {
    if (loadingInProgress) {
        show()
    } else {
        hide()
    }
}

/**
 * Update the ProgressBar progress with a smooth animation. We multiply the loadingProgress
 * by 50 so that the ProgressBar actually has some values to animate over.
 */
/*@BindingAdapter("updateProgressBarProgress")
fun ProgressBar.updateProgressBarProgress(loadingProgress: Int) {

    if (loadingProgress == 0) {
        visibility = View.VISIBLE
        return
    }

    if (loadingProgress == 1) {
        visibility = View.VISIBLE
    }

    val progressStart = (loadingProgress - 1) * 50
    val progressEnd = loadingProgress * 50

    progress = progressStart


    val animation = ObjectAnimator.ofInt(this, "progress", progressStart, progressEnd)
    animation.duration = LOADING_PROGRESS_ANIMATION_TIME
    animation.interpolator = DecelerateInterpolator()
    animation.start()
    //Wait until animation ends to hide ProgressBar
    animation.doOnEnd {
        if (progressEnd == 100) {
            visibility = View.INVISIBLE
        }
    }
}*/
