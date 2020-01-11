package com.gavinsappcreations.sunrisesunsettimes.network

import android.provider.MediaStore
import com.gavinsappcreations.sunrisesunsettimes.domain.SunData
import com.gavinsappcreations.sunrisesunsettimes.domain.TimeZoneData
import com.squareup.moshi.JsonClass

/**
 * DataTransferObjects go in this file. These are responsible for parsing responses from the server
 * or formatting objects to send to the server. We should convert these to domain objects before
 * using them.
 */

/**
 * VideoHolder holds a list of Videos.
 *
 * This is to parse first level of our network result which looks like
 *
 * {
 *   "videos": []
 * }
 */
/*@JsonClass(generateAdapter = true)
data class NetworkVideoContainer(val videos: List<NetworkVideo>)*/

/**
 * Represents data for the time zone at the location we specify in the request URL.
 */
@JsonClass(generateAdapter = true)
data class NetworkTimeZoneData(
    val dstOffset: Int,
    val rawOffset: Int,
    val status: String,
    val timeZoneId: String,
    val timeZoneName: String
)

/**
 * Convert Network results to domain objects that we can use in our app
 */
fun NetworkTimeZoneData.asDomainModel(): TimeZoneData {
    return TimeZoneData(
        dstOffset = this.dstOffset,
        rawOffset = this.rawOffset,
        status = this.status,
        timeZoneId = this.timeZoneId,
        timeZoneName = this.timeZoneName
    )
}



/**
 * SunDateContainer holds the SunData and the status.
 *
 * This is to parse first level of our network result which looks like
 *
 *  {
        "results":
        {
        "sunrise":"7:27:02 AM",
        "sunset":"5:05:55 PM",
        "solar_noon":"12:16:28 PM",
        "day_length":"9:38:53",
        "civil_twilight_begin":"6:58:14 AM",
        "civil_twilight_end":"5:34:43 PM",
        "nautical_twilight_begin":"6:25:47 AM",
        "nautical_twilight_end":"6:07:10 PM",
        "astronomical_twilight_begin":"5:54:14 AM",
        "astronomical_twilight_end":"6:38:43 PM"
        },
        "status":"OK"
    }
 */
@JsonClass(generateAdapter = true)
data class NetworkSunDataContainer(val results: NetworkSunData, val status: String)


/**
 * Represents the sunrise/sunset data at the location and time specified in the request URL.
 */
@JsonClass(generateAdapter = true)
data class NetworkSunData(
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

/**
 * Convert Network results to domain objects that we can use in our app
 */
fun NetworkSunData.asDomainModel(): SunData {
    return SunData(
        sunrise = this.sunrise,
        sunset = this.sunset,
        solar_noon = this.solar_noon,
        day_length = this.day_length,
        civil_twilight_begin = this.civil_twilight_begin,
        civil_twilight_end = this.civil_twilight_end,
        nautical_twilight_begin = this.nautical_twilight_begin,
        nautical_twilight_end = this.nautical_twilight_end,
        astronomical_twilight_begin = this.astronomical_twilight_begin,
        astronomical_twilight_end = this.astronomical_twilight_end
    )
}
