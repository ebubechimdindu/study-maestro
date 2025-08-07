package com.example.studymaestro.presentation.session

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.example.studymaestro.util.Constants.ACTION_SERVICE_CANCEL
import com.example.studymaestro.util.Constants.ACTION_SERVICE_START
import com.example.studymaestro.util.Constants.ACTION_SERVICE_STOP
import com.example.studymaestro.util.Constants.NOTIFICATION_CHANNEL_ID
import com.example.studymaestro.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.studymaestro.util.Constants.NOTIFICATION_ID
import com.example.studymaestro.util.pad
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

//In Android, services are application components that perform long-running operations in the background without providing a user interface.
@AndroidEntryPoint
class StudySessionTimerService : Service() {
    //lateinit is used because these fields are injected after the class is created.
    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder //allows you to create notifications that are compatible with older versions of Android.


    private val binder = StudySessionTimerBinder()


    private lateinit var timer: Timer

    var duration: Duration = Duration.ZERO
        private set

    var seconds = mutableStateOf("00")
        private set

    var minutes = mutableStateOf("00")
        private set

    var hours = mutableStateOf("00")
        private set

    var currentTimerState = mutableStateOf(TimerState.IDLE)
        private set

    var subjectId = mutableStateOf<Int?>(null)
        private set

    //A Bound Service is a service that allows other components (like an Activity or Fragment) to bind to it and interact with it.
    //    When the activity binds to the service, this method is called and returns your custom binder.
    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action.let {
            when (it) {
                ACTION_SERVICE_START -> {
                    startForegroundService()
                    startTimer { hours, minutes, seconds ->
                        updateNotification(
                            hours = hours,
                            minutes = minutes,
                            seconds = seconds,
                        )
                    }
                }

                ACTION_SERVICE_STOP -> {
                    stopTimer()
                }

                ACTION_SERVICE_CANCEL -> {
                    stopTimer()
                    cancelTimer()
                    stopForegroundService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        createNotificationChanel()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(NOTIFICATION_ID, notificationBuilder.build())
        } else {
            startForeground(
                NOTIFICATION_ID,
                notificationBuilder.build(),
                FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )

        }
    }

    private fun stopForegroundService() {
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }


    //    A NotificationChannel is like a category or group for notifications.
    private fun createNotificationChanel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel) //registers the channel with the system
        }
    }

    private fun startTimer(
        onTick: (h: String, m: String, s: String) -> Unit
    ) {
        currentTimerState.value = TimerState.STARTED
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            duration = duration.plus(1.seconds)
            updateTimeUnits()
            onTick(hours.value, minutes.value, seconds.value)
        }
    }

    private fun stopTimer() {
        //checked if the timer is currently running

        // Property reference to the lateinit property
        /*this::timer.isInitialized
        ✅ checks if the lateinit property has been set
        (it’s Kotlin's official way to check if a lateinit property has been initialized before using it.)*/
        if (this::timer.isInitialized) {
            timer.cancel()
        }
        currentTimerState.value = TimerState.STOPPED
    }

    private fun cancelTimer() {
        duration = Duration.ZERO
        updateTimeUnits()
        currentTimerState.value = TimerState.IDLE
    }

    private fun updateTimeUnits() {
        duration.toComponents { hours, minutes, seconds, _ ->
            this@StudySessionTimerService.hours.value = hours.toInt().pad()
            this@StudySessionTimerService.minutes.value = minutes.pad()
            this@StudySessionTimerService.seconds.value = seconds.pad()
        }
    }

    //   Dynamically updates an existing foreground notification (or creates it if it doesn't exist yet).
    private fun updateNotification(hours: String, minutes: String, seconds: String) {
        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder
                .setContentText("$hours:$minutes:$seconds")
                .build()
        )
    }

    //This is a  Binder that simply exposes the service instance (this@StudySessionTimerService) so the activity can interact with it directly.
    inner class StudySessionTimerBinder : Binder() {
        fun getService(): StudySessionTimerService = this@StudySessionTimerService
    }
}

enum class TimerState {
    IDLE,
    STARTED,
    STOPPED
}