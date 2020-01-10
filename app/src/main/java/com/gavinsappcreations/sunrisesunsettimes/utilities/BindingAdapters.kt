package com.gavinsappcreations.sunrisesunsettimes.utilities

import android.text.format.DateUtils
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.gavinsappcreations.sunrisesunsettimes.R
import java.text.SimpleDateFormat
import java.util.*


//Sets the background and enables/disables the various parts of the autocomplete Fragment
@BindingAdapter("autocompleteBackground")
fun FrameLayout.setAutoCompleteBackground (usingCustomLocation: Boolean) {
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
fun TextView.setDateText (timeInMillis: Long?) {
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