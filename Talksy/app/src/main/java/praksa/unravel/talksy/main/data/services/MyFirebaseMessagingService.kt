package praksa.unravel.talksy.main.data.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import praksa.unravel.talksy.MainActivity
import praksa.unravel.talksy.R
import praksa.unravel.talksy.main.domain.usecase.GetProfilePictureUrlUseCase

class MyFirebaseMessagingService() : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Message received: $remoteMessage")

        // Handle notification payload
        remoteMessage.notification?.let {
            val title = it.title ?: "New Message"
            val body = it.body ?: "You have a new message"
            showNotification(title, body, null)
        }

        // Handle data payload
        remoteMessage.data.let { data ->
            val title = data["title"] ?: "New Message"
            val body = data["body"] ?: "You have a new message"
            val chatId = data["chatId"] // Optional chat ID for navigation
            showNotification(title, body, chatId)
        }
    }

    fun showNotification(title: String, body: String, chatId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("chatId", chatId) // Pass chatId if available
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "MessageChannel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Message Notifications", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }



        // Build notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.dots) // Replace with your actual notification icon
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(chatId?.hashCode() ?: 0, notification) // Unique notification per chat
    }
}
