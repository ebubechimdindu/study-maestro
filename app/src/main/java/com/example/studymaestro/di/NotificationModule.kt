package com.example.studymaestro.di

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.studymaestro.R
import com.example.studymaestro.presentation.session.ServiceHelper
import com.example.studymaestro.util.Constants.NOTIFICATION_CHANNEL_ID
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped


@Module//@Module is used to indicate the koltin object or class is a dagger hilt module
@InstallIn(ServiceComponent::class)//ServiceComponent was used because the module will be used in the service class
object NotificationModule {


//    Used to build foreground notifications for your timer service.
    @ServiceScoped//@ServiceScoped means one instance per service instance.
    @Provides//The function body returns the instance Hilt should inject.
    fun provideNotificationBuilder(
        @ApplicationContext context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat
            .Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Study Session")
            .setContentText("00:00:00")
            .setSmallIcon(R.drawable.app_icon)
            .setOngoing(true)
            .setContentIntent(ServiceHelper.clickPendingIntent(context))
    }

   // Provides access to Android's NotificationManager, so your service can show or update notifications.
    @ServiceScoped
    @Provides
    fun provideNotificationMManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}