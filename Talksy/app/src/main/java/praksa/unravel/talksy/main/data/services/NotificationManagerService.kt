package praksa.unravel.talksy.main.data.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import praksa.unravel.talksy.MainActivity
import praksa.unravel.talksy.R
import praksa.unravel.talksy.main.model.Message

class NotificationManagerService(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val messageListeners = mutableMapOf<String, ListenerRegistration>()

    // Register FCM token and save to Firestore
    fun registerFCMToken() {
        if (currentUserId == null) {
            Log.e("NotificationService", "User is not authenticated.")
            return
        }

        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("NotificationService", "Retrieved FCM token: $token")

                firestore.collection("Users").document(currentUserId)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d("NotificationService", "FCM token saved successfully.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("NotificationService", "Error saving token: ${e.message}")
                    }
            } else {
                Log.e("NotificationService", "Failed to retrieve FCM token: ${task.exception?.message}")
            }
        }
    }

    // Listen for new messages in Firestore
    fun listenForNewMessages(chatId: String) {
        if (currentUserId == null) {
            Log.e("NotificationService", "User is not authenticated.")
            return
        }

        val listener = firestore.collection("Chats")
            .document(chatId)
            .collection("Messages")
            .whereNotEqualTo("senderId", currentUserId) // Exclude messages sent by the current user
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("NotificationService", "Error listening for messages: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    for (change in snapshots.documentChanges) {
                        if (change.type == DocumentChange.Type.ADDED) {
                            val message = change.document.toObject(Message::class.java)
                            Log.d("NotificationService", "New message: ${message.text}")
                            // Trigger a foreground notification
                            sendLocalNotification(message, chatId)
                        }
                    }
                }
            }

        // Store listener for cleanup later
        messageListeners[chatId] = listener
    }

    // Stop listening for messages
    fun stopListeningForMessages(chatId: String) {
        messageListeners[chatId]?.remove()
        messageListeners.remove(chatId)
    }

    // Send a local notification for foreground updates
    private fun sendLocalNotification(message: Message, chatId: String) {

        firestore.collection("Users")
            .document(message.senderId)
            .get()
            .addOnSuccessListener { document ->
                val senderName = document.getString("username") ?: "Unknown User"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("chatId", chatId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, chatId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "MessageChannel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Message Notifications", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }


        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_talksy)
            .setContentTitle("New Message: $senderName")
            .setContentText(message.text.take(50))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(chatId.hashCode(), notification)
      }
    }
}