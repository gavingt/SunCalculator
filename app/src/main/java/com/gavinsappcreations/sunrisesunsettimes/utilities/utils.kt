package com.gavinsappcreations.sunrisesunsettimes.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*


/**
 * The date string returned by api.sunrise-sunset.org is in UTC, so we convert it to
 * the correct time zone and format it the way we want it.
 */
fun formatDateResultFromApi(apiDateString: String, timeZone: TimeZone): String {

    //Parse the date string returned by the API into a Date object.
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

    //Apply the time pattern we want to show in our app.
    simpleDateFormat.applyPattern("hh:mm aa")
    return simpleDateFormat.format(correctedDate)
}



/**
 * Wait DATE_PICKER_SETTLE_TIME and if timeDateChangedInMillis doesn't change,
 * call the lambda runAfterSettling().
 */
fun waitForDatePickerToSettle(handler: Handler, timeDateChangedInMillis: Long, runAfterSettling: () -> Unit) {
    handler.removeCallbacksAndMessages(null)
    val runnable = Runnable {
        if (System.currentTimeMillis() - timeDateChangedInMillis > DATE_PICKER_SETTLE_TIME) {
            runAfterSettling()
        }
    }
    handler.postDelayed(runnable, DATE_PICKER_SETTLE_TIME)
}



//Tests if network connection is available.
fun isNetworkAvailable(context: Context?): Boolean {
    if (context == null) return false
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
                }
            }
        } else {
            try {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    return true
                }
            } catch (e: Exception) {
                Log.i("update_status", "" + e.message)
            }
        }
    return false
}