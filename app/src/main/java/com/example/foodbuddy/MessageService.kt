package com.example.foodbuddy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson

class MessageService: FirebaseMessagingService() {
    companion object {
        const val TAG = "MessageService"
    }

    private lateinit var repository: Repository
    private lateinit var gson: Gson
    private lateinit var dbLinks: DBLinks
    private var canDisplayNotification = true

    override fun onCreate() {
        super.onCreate()
        repository = Repository(this)
        gson = Gson()
        dbLinks = DBLinks()
        Log.d(TAG, "Service created")
    }

    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)
        Log.d(TAG, "onMessageReceived has been called")

        if (p0!!.data.isNotEmpty()) {
            val data = p0.data["data"]!!.subSequence(1, p0.data["data"]!!.length - 1)
            Log.d(TAG, "received message data -> $data")

            val message = gson.fromJson(data.toString(), Message::class.java)
            sendNotification(message.messageText, message.senderName)
        }
    }

    private fun sendNotification(messageBody: String, sender: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.gemini)
            .setContentTitle(sender)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}