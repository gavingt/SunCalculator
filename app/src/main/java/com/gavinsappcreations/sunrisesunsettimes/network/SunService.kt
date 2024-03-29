package com.gavinsappcreations.sunrisesunsettimes.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * A retrofit service to fetch sunrise and sunset times.
 */
interface SunService {
    @GET("json")
    suspend fun getSunData(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("date") date: String
    ): Response<NetworkSunDataContainer>
}

/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * Main entry point for network access. Call like `SunNetwork.sunData.getSunData()`
 */
object SunNetwork {
    // Configure retrofit to parse JSON and use coroutines
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.sunrise-sunset.org/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val sunData = retrofit.create(SunService::class.java)
}
