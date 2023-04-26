package com.walkingforrochester.walkingforrochester.android.ui.state

import com.google.maps.model.AutocompletePrediction

data class AddressSearchFieldState(
    val addressQuery: String = "",
    val addressSearchActive: Boolean = false,
    val predictedLocations: List<AutocompletePrediction> = listOf(),
    val loading: Boolean = false
)
