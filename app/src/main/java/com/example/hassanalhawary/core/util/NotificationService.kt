package com.example.hassanalhawary.core.util


import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Token changes sometimes. Later we can send it to Firestore if you want.
        // For now just log it if needed.
        Log.d("FCM", "New token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {

        Log.d("FCM", "Message received: $message")

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "New message"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "general"

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val large = BitmapFactory.decodeResource(resources, com.example.core.ui.R.drawable.logo)
        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.example.hassanalhawary.R.drawable.audios_icon)
            .setLargeIcon(large)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        manager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }
}