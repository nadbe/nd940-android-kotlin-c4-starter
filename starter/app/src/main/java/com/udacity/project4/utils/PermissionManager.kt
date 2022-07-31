package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R

private const val TAG = "PermissionManager"

class PermissionManager {

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    @TargetApi(29)
    fun requestForegroundAndBackgroundLocationPermissions(requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {

        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }
        }
        requestPermissionLauncher.launch(permissionsArray)
    }

    // ACCESS_FINE_LOCATION
    // ACCESS_BACKGROUND_LOCATION
    @TargetApi(29)
    fun foregroundAndBackgroundLocationPermissionApproved(context: Context): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }




    fun isLocationPermissionGranted(context: Context) : Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        const val LOCATION_PERMISSION_INDEX = 0
    }

}