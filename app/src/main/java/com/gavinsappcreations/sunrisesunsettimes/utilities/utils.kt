package com.gavinsappcreations.sunrisesunsettimes.utilities

import java.text.SimpleDateFormat
import java.util.*



/**
 * The date string returned by api.sunrise-sunset.org is in UTC, so we convert it to
 * the correct time zone and format it the way we want it.
 */
fun formatDateResultFromApi(apiDateString: String, timeZone: TimeZone): String {

    /**
     * Parse the date string returned by the API into a Date object.
     */
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa", Locale.ENGLISH)
    val apiDate = simpleDateFormat.parse(apiDateString)

    /**
     * Use a calendar to turn the UTC time returned by the API into the current time for the
     * location of interest. Do this by adding timeZone.getOffset to apiDate
     */
    val calendar = Calendar.getInstance()
    calendar.time = apiDate!!
    calendar.add(Calendar.MILLISECOND, timeZone.getOffset(calendar.timeInMillis))
    val correctedDate = calendar.time

    /**
     * Apply the time pattern we want to show in our app
     */
    simpleDateFormat.applyPattern("hh:mm aa")
    return simpleDateFormat.format(correctedDate)
}