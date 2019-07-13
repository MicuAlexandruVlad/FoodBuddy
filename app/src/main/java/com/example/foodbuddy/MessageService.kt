package com.example.foodbuddy

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.os.AsyncTask
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
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
    private lateinit var lastTextMessage: Message
    private lateinit var conversationIds: ArrayList<String>
    private var canDisplayNotification = false

    override fun onCreate() {
        super.onCreate()
        repository = Repository(this)
        gson = Gson()
        dbLinks = DBLinks()
        lastTextMessage = Message()
        conversationIds = ArrayList()
        getConversationIds(conversationIds)
        getCurrentUser()
        Log.d(TAG, "Service created")

        LocalBroadcastManager.getInstance(this).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                canDisplayNotification = intent.getBooleanExtra("notify", false)
                Log.d(TAG, "onCreate: allow notification -> $canDisplayNotification")
            }
        }, IntentFilter("display-notification"))

        LocalBroadcastManager.getInstance(this).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                lastTextMessage = intent.getSerializableExtra("message") as Message
            }
        }, IntentFilter("inserted-message"))
    }

    private fun getCurrentUser() {

    }

    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)
        Log.d(TAG, "onMessageReceived has been called")

        if (p0!!.data.isNotEmpty()) {
            val data = p0.data["data"]!!.subSequence(1, p0.data["data"]!!.length - 1)
            Log.d(TAG, "received message data -> $data")

            val message = gson.fromJson(data.toString(), Message::class.java)
            if (message.type == Message.MESSAGE_TEXT) {
                message.conversationId = message.senderId
                // TODO: set the ownerID. But first store current
                //  user id...or maybe all the data in Room
                if (lastTextMessage.senderId.compareTo(message.senderId, false) != 0) {
                    // message is not sent by the current user
                    repository.insertMessage(message)
                    broadcastMessage(message)
                    if (canDisplayNotification)
                        sendNotification(message.messageText, message.senderName)
                }
            }
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

    @SuppressLint("StaticFieldLeak")
    private fun getConversationIds(ids: ArrayList<String>) {
        object : AsyncTask<Void, Void, Long>() {
            override fun doInBackground(vararg voids: Void): Long? {
                ids.addAll(repository.getConversationIds())

                return 0
            }

            override fun onPostExecute(id: Long?) {
                Log.d(Repository.TAG, "onPostExecute: distinct conversation ids -> " + ids.size)
            }
        }.execute()
    }

    private fun broadcastMessage(message: Message) {
        val intent = Intent("new-message")
        intent.putExtra("message", message)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}