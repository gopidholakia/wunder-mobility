package com.wunder.rental.ui.map

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.LocationManager
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


internal fun Fragment.checkLocationPermission(): Boolean {
    return ActivityCompat.checkSelfPermission(
        requireContext().applicationContext,
        ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        requireContext().applicationContext,
        ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

internal fun Fragment.isGpsProviderEnable(): Boolean {
    return (activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
        .isProviderEnabled(LocationManager.GPS_PROVIDER)
}


internal fun requestLocationPermission(
    locationPermissionRequest: ActivityResultLauncher<Array<String>>
) {
    locationPermissionRequest.launch(
        arrayOf(
            ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION
        )
    )
}

internal fun Fragment.askForGPSAccess() {
    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(createLocationRequest())
    val client: SettingsClient = LocationServices.getSettingsClient(requireContext())
    val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
    task.addOnFailureListener {
        if (it is ResolvableApiException) {
            try {
                it.startResolutionForResult(
                    requireActivity(),
                    1002
                )
            } catch (sendEx: IntentSender.SendIntentException) {
            }
        }
    }
}

internal fun createLocationRequest(): LocationRequest {
    return LocationRequest.create().apply {
        interval = 2000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
}

internal fun getBitmapFromView(customMarkerView: View): Bitmap {
    customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    customMarkerView.layout(
        customMarkerView.measuredWidth,
        customMarkerView.measuredHeight,
        customMarkerView.measuredWidth,
        customMarkerView.measuredHeight
    )
    customMarkerView.buildLayer()
    val bitmap: Bitmap = Bitmap.createBitmap(
        customMarkerView.measuredWidth, customMarkerView.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.BLACK, PorterDuff.Mode.SRC_IN)
    val drawable = customMarkerView.background
    drawable?.draw(canvas)
    customMarkerView.draw(canvas)
    return bitmap
}

