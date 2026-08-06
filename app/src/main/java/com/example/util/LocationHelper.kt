package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

object LocationHelper {
    // Bally Jute Mill Premises Anchor Coordinates (Bally, Howrah, West Bengal)
    const val BALLY_MILL_LAT = 22.6500
    const val BALLY_MILL_LNG = 88.3400
    const val DEFAULT_GEOFENCE_RADIUS_METERS = 500.0

    fun calculateDistanceMeters(
        startLat: Double, startLng: Double,
        endLat: Double = BALLY_MILL_LAT, endLng: Double = BALLY_MILL_LNG
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLng, endLat, endLng, results)
        return results[0]
    }

    fun isWithinBallyMillGeofence(
        currentLat: Double,
        currentLng: Double,
        radiusMeters: Double = DEFAULT_GEOFENCE_RADIUS_METERS
    ): Boolean {
        val distance = calculateDistanceMeters(currentLat, currentLng)
        return distance <= radiusMeters
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        radiusMeters: Int = 500,
        onSuccess: (lat: Double, lng: Double, distanceMeters: Float, isInsideMill: Boolean, locationName: String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val distance = calculateDistanceMeters(location.latitude, location.longitude)
                        val isInside = distance <= radiusMeters
                        val locName = if (isInside) {
                            "Bally Jute Mill Premises, Howrah (GPS Verified)"
                        } else {
                            "Outside Mill Premises (${distance.toInt()}m from Bally Mill)"
                        }
                        onSuccess(location.latitude, location.longitude, distance, isInside, locName)
                    } else {
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                val distance = calculateDistanceMeters(lastLoc.latitude, lastLoc.longitude)
                                val isInside = distance <= radiusMeters
                                val locName = if (isInside) {
                                    "Bally Jute Mill Premises (Last Known GPS)"
                                } else {
                                    "Outside Mill Premises (${distance.toInt()}m from Mill)"
                                }
                                onSuccess(lastLoc.latitude, lastLoc.longitude, distance, isInside, locName)
                            } else {
                                onError("Google Location Services returned null location.")
                            }
                        }.addOnFailureListener {
                            onError("Failed to get last known location: ${it.localizedMessage}")
                        }
                    }
                }
                .addOnFailureListener {
                    onError("Google Location Services error: ${it.localizedMessage}")
                }
        } catch (e: Exception) {
            onError("Location exception: ${e.localizedMessage}")
        }
    }
}
