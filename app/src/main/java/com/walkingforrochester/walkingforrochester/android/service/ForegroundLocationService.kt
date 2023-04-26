package com.walkingforrochester.walkingforrochester.android.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.walkingforrochester.walkingforrochester.android.BuildConfig
import com.walkingforrochester.walkingforrochester.android.MainActivity
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.model.LocationTrackingEvent
import com.walkingforrochester.walkingforrochester.android.model.LocationTrackingEventType
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ForegroundLocationService : Service() {

    private val localBinder = LocalBinder()
    private lateinit var notificationManager: NotificationManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        Timber.d("onCreate()")

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(TimeUnit.SECONDS.toMillis(10))
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5))
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateDistanceMeters(1f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                EventBus.getDefault().post(
                    LocationTrackingEvent(
                        event = LocationTrackingEventType.Location,
                        locations = locationResult.locations
                    )
                )
                Timber.d("Sent out Location event: ${locationResult.locations}")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return localBinder
    }

    private fun generateNotification(): Notification {
        val mainNotificationText = getString(R.string.walking_notification)
        val titleText = getString(R.string.app_name)
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(notificationChannel)

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        val launchActivityIntent = Intent(this, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(this, MainActivity::class.java)
            .putExtra(EXTRA_STOP_WALKING, true)
        val cancelPendingIntent = PendingIntent.getActivity(
            this,
            -1,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

        return notificationCompatBuilder
            .setSilent(true)
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setContentIntent(activityPendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_stop,
                getString(R.string.stop_walking),
                cancelPendingIntent
            )
            .build()
    }

    fun subscribeToLocationUpdates() {
        Timber.d("subscribeToLocationUpdates()")

        // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
        // ensure this Service can be promoted to a foreground service, i.e., the service needs to
        // be officially started (which we do here).
        startService(Intent(applicationContext, ForegroundLocationService::class.java))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                generateNotification(),
                FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, generateNotification())
        }

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            Timber.e("Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    fun unsubscribeToLocationUpdates() {
        Timber.d("unsubscribeToLocationUpdates()")

        try {
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Location Callback removed.")
                    stopSelf()
                } else {
                    Timber.d("Failed to remove Location Callback.")
                }
            }

            stopForeground(STOP_FOREGROUND_REMOVE)

        } catch (unlikely: SecurityException) {
            Timber.e("Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    fun stopFromIntent() {
        EventBus.getDefault().post(LocationTrackingEvent(event = LocationTrackingEventType.Stop))
    }

    inner class LocalBinder : Binder() {
        internal val service: ForegroundLocationService
            get() = this@ForegroundLocationService
    }

    companion object {

        const val EXTRA_STOP_WALKING = "${BuildConfig.APPLICATION_ID}.extra.STOP_WALKING"

        const val NOTIFICATION_ID = 1983743982

        const val NOTIFICATION_CHANNEL_ID = "wfr_log_a_walk_channel"

    }

}