package com.vexa.konumgonder.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHelper(context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        val cancellationTokenSource = CancellationTokenSource()
        val currentLocation = awaitTask(
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )
        )
        return currentLocation ?: awaitTask(fusedLocationClient.lastLocation)
    }

    private suspend fun <T> awaitTask(task: Task<T>): T? = suspendCancellableCoroutine { continuation ->
        task.addOnSuccessListener { result ->
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }.addOnFailureListener {
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }.addOnCanceledListener {
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }
    }

    companion object {
        fun createGoogleMapsUrl(latitude: Double, longitude: Double): String {
            return "https://maps.google.com/?q=$latitude,$longitude"
        }
    }
}
