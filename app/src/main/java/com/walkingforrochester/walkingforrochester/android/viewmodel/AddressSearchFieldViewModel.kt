package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.PlaceAutocompleteRequest
import com.google.maps.PlacesApi
import com.google.maps.model.AutocompletePrediction
import com.google.maps.model.LatLng
import com.google.maps.model.PlaceDetails
import com.walkingforrochester.walkingforrochester.android.BuildConfig
import com.walkingforrochester.walkingforrochester.android.model.LocationTrackingEvent
import com.walkingforrochester.walkingforrochester.android.model.LocationTrackingEventType
import com.walkingforrochester.walkingforrochester.android.showUnexpectedErrorToast
import com.walkingforrochester.walkingforrochester.android.ui.state.AddressSearchFieldState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddressSearchFieldViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressSearchFieldState())
    val uiState = _uiState.asStateFlow()

    private var geoLocation: LatLng? = null
    private var geoContext: GeoApiContext? = null
    private var geoSession: PlaceAutocompleteRequest.SessionToken? = null

    fun onAddressQueryChange(newAddressQuery: String) = flow<Nothing> {
        _uiState.update { it.copy(addressQuery = newAddressQuery) }

        searchAddress(newAddressQuery)
    }.launchIn(viewModelScope)

    private fun searchAddress(query: String) {
        if (query.isEmpty()) {
            _uiState.update { it.copy(predictedLocations = listOf()) }
            return
        }

        _uiState.update { it.copy(loading = true) }

        val request = PlacesApi.placeAutocomplete(geoContext, query, geoSession)
        geoLocation?.let { request.location(it).radius(10 * 1000) }

        request.setCallback(object : PendingResult.Callback<Array<AutocompletePrediction?>?> {
            override fun onResult(result: Array<AutocompletePrediction?>?) {
                val predictedLocations = mutableListOf<AutocompletePrediction>()
                result?.forEach { location ->
                    location?.let {
                        predictedLocations.add(it)
                    }
                }

                _uiState.update {
                    it.copy(
                        predictedLocations = predictedLocations,
                        loading = false
                    )
                }
            }

            override fun onFailure(e: Throwable?) {
                Timber.e(e, "Unable to search address")

                Handler(Looper.getMainLooper()).post {
                    showUnexpectedErrorToast(context)
                }
            }
        })
    }

    fun onAddressSelect(placeId: String) = flow<Nothing> {
        _uiState.update { it.copy(addressSearchActive = false, loading = true) }

        PlacesApi.placeDetails(geoContext, placeId, geoSession)
            .setCallback(object : PendingResult.Callback<PlaceDetails?> {
                override fun onResult(result: PlaceDetails?) {
                    result?.geometry?.location?.let { location ->
                        EventBus.getDefault().post(
                            LocationTrackingEvent(
                                event = LocationTrackingEventType.SelectedAddress,
                                selectedAddress = com.google.android.gms.maps.model.LatLng(
                                    location.lat,
                                    location.lng
                                )
                            )
                        )
                        _uiState.update { it.copy(loading = false) }
                    }
                }

                override fun onFailure(e: Throwable?) {
                    Timber.e(e, "Unable to select address")

                    Handler(Looper.getMainLooper()).post {
                        showUnexpectedErrorToast(context)
                    }
                }

            })

        onAddressSearchActiveChange(false)
    }.launchIn(viewModelScope)

    fun onAddressSearchActiveChange(addressSearchActive: Boolean) = flow<Nothing> {
        if (addressSearchActive) {
            geoContext =
                GeoApiContext.Builder().apiKey(BuildConfig.googleMapsKey).build()
            geoSession = PlaceAutocompleteRequest.SessionToken()

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        geoLocation = LatLng(it.latitude, it.longitude)
                    }
                }
            }
        } else {
            geoLocation = null
            geoContext = null
            geoSession = null
        }
        _uiState.update { it.copy(addressSearchActive = addressSearchActive) }
    }.launchIn(viewModelScope)

}