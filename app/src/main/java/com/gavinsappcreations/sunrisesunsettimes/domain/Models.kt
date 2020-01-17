package com.gavinsappcreations.sunrisesunsettimes.domain


/**
 * Domain objects are plain Kotlin data classes that represent the things in our app. These are the
 * objects that should be displayed on screen, or manipulated by the app.
 *
 * @see database for objects that are mapped to the database
 * @see network for objects that parse or prepare network calls
 */

/**
 * TimeZoneData represents the data for the time zone at the location specified in the request URL.
 */
data class TimeZoneData(
    val dstOffset: Int,
    val rawOffset: Int,
    val status: String,
    val timeZoneId: String,
    val timeZoneName: String
)


/**
 * SunData represents the sunrise/sunset data at the location and time specified in the request URL.
 */
data class SunData(
    val sunrise: String,
    val sunset: String,
    val solar_noon: String,
    val day_length: String,
    val civil_twilight_begin: String,
    val civil_twilight_end: String,
    val nautical_twilight_begin: String,
    val nautical_twilight_end: String,
    val astronomical_twilight_begin: String,
    val astronomical_twilight_end: String
)