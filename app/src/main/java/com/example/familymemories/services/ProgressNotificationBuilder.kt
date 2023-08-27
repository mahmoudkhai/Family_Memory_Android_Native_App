package com.example.familymemory.services

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.familymemory.R

class ProgressNotificationBuilder(
    private val context: Context,
    private val channerId: String,
    private val notificationID: Int,
) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(title: String, message: String, progress: Int) {
        val notification = NotificationCompat.Builder(context, channerId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setProgress(100, progress, false)
            .setOnlyAlertOnce(true)
            .build()

        notificationManager.notify(notificationID, notification)

    }
    fun dismissNotification() {
        notificationManager.cancel(notificationID)
    }
}