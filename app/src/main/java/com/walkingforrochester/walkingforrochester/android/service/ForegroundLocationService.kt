package com.walkingforrochester.walkingforrochester.android.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.walkingforrochester.walkingforrochester.android.BuildConfig
import com.walkingforrochester.walkingforrochester.android.MainActivity
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.di.DefaultDispatcher
import com.walkingforrochester.walkingforrochester.android.model.WalkData.WalkState
import com.walkingforrochester.walkingforrochester.android.repository.WalkRepository
import com.walkingforrochester.walkingforrochester.android.showNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundLocationService : LifecycleService() {

    @Inject
    lateinit var walkRepository: WalkRepository

    @Inject
    @DefaultDispatcher
    lateinit var defaultDispatcher: CoroutineDispatcher

    private lateinit var notificationManager: NotificationManagerCompat

    private var serviceStarted = false

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate()")

        val locationLifecycleObserver = LocationLifecycleObserver(
            application = this.application,
            walkRepository = walkRepository,
            defaultDispatcher = defaultDispatcher
        )
        lifecycle.addObserver(locationLifecycleObserver)

        notificationManager = NotificationManagerCompat.from(this)

        val context = this

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                walkRepository.walkData.collect {
                    // Keep service active during submission as temp permissions will kill process
                    // TODO Better solution would be saving to persistent storage
                    if (it.state == WalkState.IN_PROGRESS || it.state == WalkState.COMPLETE) {
                        startService()
                    } else {
                        stopService()

                        when (it.state) {
                            WalkState.MOCK_LOCATION_DETECTED -> {
                                showNotification(
                                    context = context,
                                    messageResId = R.string.mock_location_dialog
                                )
                            }

                            else -> {
                                notificationManager.cancel(NOTIFICATION_ID)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun generateNotification(): Notification {
        val mainNotificationText = getString(R.string.walking_notification)
        val titleText = getString(R.string.notification_channel_name)
        val notificationChannel = NotificationChannelCompat.Builder(
            NOTIFICATION_CHANNEL_ID,
            NotificationManager.IMPORTANCE_LOW
        )
            .setName(titleText)
            .build()

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

        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

        return notificationCompatBuilder
            .setSilent(true)
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setContentIntent(activityPendingIntent)
            .setSmallIcon(R.drawable.ic_notification)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun startService() {
        if (!serviceStarted) {
            Timber.d("startService")

            // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
            // ensure this Service can be promoted to a foreground service, i.e., the service needs to
            // be officially started (which we do here).
            startService(Intent(applicationContext, ForegroundLocationService::class.java))

            val notification = generateNotification()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }

            serviceStarted = true
        }
    }

    private fun stopService() {
        if (serviceStarted) {

            Timber.d("stopService()")

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            serviceStarted = false
        }
    }

    companion object {

        const val NOTIFICATION_ID = 1983743982

        const val NOTIFICATION_CHANNEL_ID = "wfr_log_a_walk_channel"

    }

}