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
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.json.JSONObject
import java.lang.StringBuilder
import java.net.URISyntaxException

class MessageService: FirebaseMessagingService() {
    companion object {
        const val TAG = "MessageService"
    }

    private lateinit var repository: Repository
    private lateinit var gson: Gson
    private lateinit var dbLinks: DBLinks
    private lateinit var lastTextMessage: Message
    private lateinit var conversationIds: ArrayList<String>
    private lateinit var conversationIdsString: String
    private lateinit var socket: Socket
    private var canDisplayNotification = false

    override fun onCreate() {
        super.onCreate()
        repository = Repository(this)
        gson = Gson()
        dbLinks = DBLinks()
        lastTextMessage = Message()
        conversationIds = ArrayList()
        conversationIdsString = ""

        Log.d(TAG, "Service created")

        try {
            socket = IO.socket(dbLinks.socketLink)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
        socket.connect()
        getConversationIds(conversationIds)

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

    private fun listenForUserStatusChange(userId: String) {
        Log.d(TAG, "Starting to listen for status change for user with id -> $userId")
        socket.on(SocketEvents.listenForUserStatusChange(userId)) { args ->
            val data = args[0] as JSONObject
            Log.d(TAG, "Status changed for this user")
            Log.d(TAG, "Data -> $data")

            // TODO: update userStatus

            val userStatus = gson.fromJson(data.toString(), UserStatus::class.java) as UserStatus
            broadcastStatusChange(userStatus)
            Thread {
                repository.updateUserStatus(userStatus.userId, userStatus.status, userStatus.statusChangedAt)
            }.start()

        }

    }

    private fun broadcastStatusChange(userStatus: UserStatus) {
        val intent = Intent("status-changed")
        intent.putExtra("user-status", userStatus)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)
        Log.d(TAG, "onMessageReceived has been called")

        if (p0!!.data.isNotEmpty()) {
            val data = p0.data["data"]!!.subSequence(1, p0.data["data"]!!.length - 1)
            Log.d(TAG, "received message data -> $data")

            val message = gson.fromJson(data.toString(), Message::class.java)
            if (message.senderId !in conversationIds && lastTextMessage.senderId.compareTo(message.senderId, false) != 0) {
                conversationIds.add(message.senderId)
                Log.d(TAG, "Conversations updated -> " + conversationIds.size)
                val userStatus = UserStatus()
                userStatus.userId = message.senderId
                repository.insertUserStatus(userStatus)
                listenForUserStatusChange(message.senderId)
                Log.d(TAG, "line 115: New conversation with -> " + message.senderId)

                //TODO: Broadcast new conversation... a new user is trying to contact me
            }
            if (lastTextMessage.senderId.compareTo(message.senderId, false) == 0) {
               if (message.conversationId !in conversationIds) {
                   conversationIds.add(message.conversationId)
                   val userStatus = UserStatus()
                   userStatus.userId = message.conversationId
                   repository.insertUserStatus(userStatus)
                   listenForUserStatusChange(message.conversationId)
                   Log.d(TAG, "line 126: New conversation with -> " + message.conversationId)
               }
            }
            if (message.type == Message.MESSAGE_TEXT) {
                // TODO: set the ownerID. But first store current
                //  user id...or maybe all the data in Room
                if (lastTextMessage.senderId.compareTo(message.senderId, false) != 0) {
                    // message is not sent by the current user
                    message.conversationId = message.senderId
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
                Log.d(TAG, "onPostExecute: distinct conversation ids -> " + ids.size)
                if (conversationIds.isNotEmpty()) {
                    for (index in 0 until conversationIds.size) {
                        listenForUserStatusChange(conversationIds[index])
                    }
                }
            }
        }.execute()
    }

    private fun broadcastMessage(message: Message) {
        val intent = Intent("new-message")
        intent.putExtra("message", message)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }


}