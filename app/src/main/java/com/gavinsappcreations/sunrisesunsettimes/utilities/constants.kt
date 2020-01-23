package com.gavinsappcreations.sunrisesunsettimes.utilities

// TODO: Add a valid Google API key here that has both the Places and TimeZone APIs activated.
const val GOOGLE_PLACES_AND_TIMEZONE_API_KEY = ""

const val MILLISECONDS_PER_MINUTE = 60000
const val REQUEST_PERMISSIONS_LOCATION_ONLY_REQUEST_CODE = 1

/**
 * This is the time in milliseconds between OnDateChangedListener callbacks that we wait before we
 * consider the user to have settled on a date. This prevents unnecessary API calls from the user
 * spinning the DatePicker to reach a certain date.
 */
const val DATE_PICKER_SETTLE_TIME: Long = 350

// This is the amount of time in ms that it takes to animate each chunk of the loading bar.
const val LOADING_PROGRESS_ANIMATION_TIME: Long = 350
