package com.gavinsappcreations.sunrisesunsettimes.network

import android.provider.MediaStore
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
    val timeZoneName: String)

/**
 * Convert Network results to domain objects that we can use in our app
 */
fun NetworkTimeZoneData.asDomainModel(): TimeZoneData {
        return TimeZoneData(
            dstOffset = this.dstOffset,
            rawOffset = this.rawOffset,
            status = this.status,
            timeZoneId = this.timeZoneId,
            timeZoneName = this.timeZoneName)
}

