package com.gavinsappcreations.sunrisesunsettimes.utilities

const val MILLISECONDS_PER_MINUTE = 60000
const val REQUEST_PERMISSIONS_LOCATION_ONLY_REQUEST_CODE = 1
const val PLACES_API_KEY = "AIzaSyCiNoSDVQtYBByS97Mou3v0k3o_1hR38qE"

/**
 * This is the time in milliseconds between OnDateChangedListener callbacks that we wait before we
 * consider the user to have settled on a date. This prevents unnecessary API calls from the user
 * spinning the DatePicker to reach a certain date.
 */
const val DATE_PICKER_SETTLE_TIME: Long = 350