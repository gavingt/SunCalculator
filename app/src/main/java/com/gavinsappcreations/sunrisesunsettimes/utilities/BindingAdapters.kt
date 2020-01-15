package com.gavinsappcreations.sunrisesunsettimes.utilities

import android.animation.ObjectAnimator
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

const val LOADING_PROGRESS_ANIMATION_TIME: Long = 350  //350 ms

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
@BindingAdapter("setSunViewVisibility")
fun View.setSunViewVisibility(loadingProgress: Int) {
    when (loadingProgress) {
        0, 1 -> visibility = View.INVISIBLE
        else -> {
            postDelayed({
                visibility = View.VISIBLE
            }, LOADING_PROGRESS_ANIMATION_TIME)
        }
    }
}

/**
 * Update the ProgressBar progress with a smooth animation. We multiply the loadingProgress
 * by 50 so that the ProgressBar actually has some values to animate over.
 */
@BindingAdapter("updateProgressBarProgress")
fun ContentLoadingProgressBar.updateProgressBarProgress(loadingProgress: Int) {

    if (loadingProgress == 0 || loadingProgress == 1) {
        show()
    }

    val animation = ObjectAnimator.ofInt(this, "progress", loadingProgress * 50)
    animation.duration = LOADING_PROGRESS_ANIMATION_TIME
    animation.interpolator = DecelerateInterpolator()
    animation.start()
    //Wait until animation ends to hide ProgressBar
    animation.doOnEnd {
        if (loadingProgress == 2) {
            hide()
        }
    }
}


//Controls the visibility of the TextView that's above the ProgressBar.
@BindingAdapter("setLoadingTextVisibility")
fun TextView.setLoadingTextVisibility(loadingProgress: Int) {
    when (loadingProgress) {
        0, 1 -> visibility = View.VISIBLE
        2 -> {
            postDelayed({
                visibility = View.INVISIBLE
            }, LOADING_PROGRESS_ANIMATION_TIME)
        }
    }
}