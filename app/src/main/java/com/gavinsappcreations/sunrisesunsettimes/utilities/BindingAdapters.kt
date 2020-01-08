package com.gavinsappcreations.sunrisesunsettimes.utilities

import android.view.View
import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.adapters.DatePickerBindingAdapter

/**
 * Binding adapter used to hide the autocompleteFrameLayout if using current location
 */
@BindingAdapter("visibleIfUsingCustomLocation")
fun FrameLayout.visibleIfUsingCustomLocation (it: Boolean) {
    visibility = if (it) View.GONE else View.VISIBLE
}