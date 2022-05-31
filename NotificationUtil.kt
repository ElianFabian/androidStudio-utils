package com.elian.efficientneuron.util.extension

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationUtil(
    private val context: Context,
    val channelId: String,
    val channelName: String,
    val importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
)
{
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notification: Notification

    fun showNotification()
    {
        notificationManager.notify(0, notification)
    }

    fun createNotificationChannel(lambdaChannel: NotificationChannel.() -> Unit)
    {
        notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

		val channel = NotificationChannel(channelId, channelName, importance).apply()
		{
			lambdaChannel()
		}

		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		notificationManager.createNotificationChannel(channel)
    }

    fun createNotification(lambdaNotificationBuilder: NotificationCompat.Builder.() -> Unit)
    {
        notification = NotificationCompat.Builder(context, channelId).also(lambdaNotificationBuilder).build()
    }
}