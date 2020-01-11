package com.gavinsappcreations.sunrisesunsettimes.network

import com.gavinsappcreations.sunrisesunsettimes.PLACES_API_KEY
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * A retrofit service to fetch TimeZone data.
 */
interface TimeZoneService {
    @GET("maps/api/timezone/json")
    fun getTimeZoneData(
        @Query("location") latLng: String,
        @Query("timestamp") dateInMillis: Long,
        @Query("key") apiKey: String
    ) : Deferred<NetworkTimeZoneData>
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
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val timeZone = retrofit.create(TimeZoneService::class.java)
}
