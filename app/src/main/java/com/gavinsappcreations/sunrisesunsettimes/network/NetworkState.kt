package com.gavinsappcreations.sunrisesunsettimes.network

sealed class NetworkState {

    // Network calls were successful.
    object NetworkSuccess : NetworkState()

    // Network error has occurred.
    object NetworkFailure : NetworkState()

    // App just started and is awaiting location permission.
    object AwaitingPermission: NetworkState()



    // Network is actively loading. The "progress" value can be either 0 or 1.
    class NetworkLoading(val progress: Int) : NetworkState()
}