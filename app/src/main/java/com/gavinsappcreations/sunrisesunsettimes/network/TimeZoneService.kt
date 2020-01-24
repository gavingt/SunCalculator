package com.gavinsappcreations.sunrisesunsettimes.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * A retrofit service to fetch TimeZone data.
 */
interface TimeZoneService {
    @GET("maps/api/timezone/json")
    suspend fun getTimeZoneData(
        @Query("location") latLng: String,
        @Query("timestamp") dateInMillis: Long,
        @Query("key") apiKey: String
    ): Response<NetworkTimeZoneData>
}

/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * Main entry point for network access. Call like `TimeZoneNetwork.timeZone.getTimeZoneData()`
 */
object TimeZoneNetwork {
    // Configure retrofit to parse JSON and use coroutines
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val timeZone = retrofit.create(TimeZoneService::class.java)
}
