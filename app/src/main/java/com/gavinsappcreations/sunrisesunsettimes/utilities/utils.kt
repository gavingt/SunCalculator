package com.gavinsappcreations.sunrisesunsettimes.utilities

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*


/**
 * The date string returned by api.sunrise-sunset.org is in UTC, so we convert it to
 * the correct time zone and format it the way we want it.
 */
fun formatDateResultFromApi(apiDateString: String, timeZone: TimeZone): String {

    // Parse the date string returned by the API into a Date object.
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

    // Apply the time pattern we want to show in our app.
    simpleDateFormat.applyPattern("hh:mm aa")
    return simpleDateFormat.format(correctedDate)
}


/**
 * Wait DATE_PICKER_SETTLE_TIME and if timeDateChangedInMillis doesn't change,
 * call the lambda runAfterSettling().
 */
fun waitForDatePickerToSettle(
    handler: Handler,
    timeDateChangedInMillis: Long,
    runAfterSettling: () -> Unit
) {
    handler.removeCallbacksAndMessages(null)
    val runnable = Runnable {
        if (System.currentTimeMillis() - timeDateChangedInMillis > DATE_PICKER_SETTLE_TIME) {
            runAfterSettling()
        }
    }
    handler.postDelayed(runnable, DATE_PICKER_SETTLE_TIME)
}


// Tests if network connection is available
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


// We use this method to get the default text color programmatically
fun resolveThemeAttr(context: Context, @AttrRes attrRes: Int): TypedValue {
    val theme = context.theme
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue
}

// We use this method to get the default text color programmatically
@ColorInt
fun resolveColorAttr(context: Context, @AttrRes colorAttr: Int): Int {
    val resolvedAttr = resolveThemeAttr(context, colorAttr)
    // resourceId is used if it's a ColorStateList, and data if it's a color reference or a hex color
    val colorRes = if (resolvedAttr.resourceId != 0)
        resolvedAttr.resourceId
    else
        resolvedAttr.data
    return ContextCompat.getColor(context, colorRes)
}