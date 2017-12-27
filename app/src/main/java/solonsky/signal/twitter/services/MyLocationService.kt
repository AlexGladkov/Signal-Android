package solonsky.signal.twitter.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import solonsky.signal.twitter.helpers.AppData

/**
 * Created by neura on 02.11.17.
 */

class MyLocationService: Service() {
    val TAG = MyLocationService::class.java.simpleName
    var mLocationManager: LocationManager? = null
    private val locationInterval: Long = 1000
    private val locationDistance: Float = 10f

    class LocationListener(provider: String) : android.location.LocationListener {
        var mLastLocation: Location = Location(provider)
        val TAG = LocationListener::class.java.simpleName

        override fun onLocationChanged(location: Location?) {
            Log.e(TAG, "location changed")
            mLastLocation.set(location)
            AppData.currentLocation = location
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            Log.e(TAG, "onStatusChanged - " + provider)
        }

        override fun onProviderEnabled(provider: String?) {
            Log.e(TAG, "onProviderEnabled - " + provider)
        }

        override fun onProviderDisabled(provider: String?) {
            Log.e(TAG, "onProviderDisabled - " + provider)
        }

    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        initializeLocationManager()
        Log.e(TAG, "Service created")

        try {
            mLocationManager?.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER,
                    locationInterval, locationDistance,
                    LocationListener(LocationManager.PASSIVE_PROVIDER))
            Log.e(TAG, "Location updates requested")
        } catch (err: Exception) {
            Log.e(TAG, "exception - " + err.localizedMessage)
        }
    }

    private fun initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        }
    }
}