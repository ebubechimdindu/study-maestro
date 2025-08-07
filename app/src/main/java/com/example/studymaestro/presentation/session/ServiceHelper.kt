package com.example.studymaestro.presentation.session

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.example.studymaestro.MainActivity
import com.example.studymaestro.util.Constants.CLICK_REQUEST_CODE


object ServiceHelper {
    fun clickPendingIntent(context: Context): PendingIntent {
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,// Standard action for viewing content
            "study_maestro://dashboard/session".toUri(),
            context,//Current context
            MainActivity::class.java//Target activity class
        )
//        TaskStackBuilder creates the proper navigation hierarchy.
        return TaskStackBuilder.create(context).run {
            //addNextIntentWithParentStack(): Automatically adds parent activities to the back stack based on your manifest's android:parentActivityName declarations
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(
                CLICK_REQUEST_CODE,//Unique identifier for this PendingIntent
                PendingIntent.FLAG_IMMUTABLE//Required for API 31+, makes the PendingIntent immutable for security
            )
        }
    }

    fun triggerForegroundService(context: Context, action: String) {
        Intent(context, StudySessionTimerService::class.java).apply {
            this.action = action
            context.startService(this)
        }
    }
}