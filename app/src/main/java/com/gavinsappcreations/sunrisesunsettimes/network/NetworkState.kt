package com.gavinsappcreations.sunrisesunsettimes.network

sealed class NetworkState {

    // Network calls were successful.
    object NetworkSuccess : NetworkState()

    // Network error has occurred.
    object NetworkFailure : NetworkState()

    // App just started and is awaiting location permission.
    object AwaitingPermission: NetworkState()

    // User denied location permission.
    object PermissionDenied: NetworkState()

    // User has disabled Location on device and is trying to fetch current location.
    object LocationDisabled: NetworkState()

    // Network is actively loading. The "progress" value can be either 0 or 1.
    class NetworkLoading(val progress: Int) : NetworkState()
}