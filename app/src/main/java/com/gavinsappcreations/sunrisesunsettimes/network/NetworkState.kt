package com.gavinsappcreations.sunrisesunsettimes.network

sealed class NetworkState {
    object NetworkSuccess : NetworkState()  // Network calls were successful
    object NetworkFailure : NetworkState()  // Network error occurred
    object NetworkAwaitingPermission: NetworkState() // App just started and awaiting permission
    class NetworkLoading(val progress: Int) : NetworkState() // Progress can be either 0 or 1
}