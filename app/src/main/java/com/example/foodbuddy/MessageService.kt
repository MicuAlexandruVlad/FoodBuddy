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
import android.view.View
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.json.JSONObject
import java.lang.StringBuilder
import java.net.URISyntaxException
import java.util.*
import kotlin.collections.ArrayList

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
    private var currentUser = User()
    private var canDisplayNotification = false
    private var userRetrieved = false

    override fun onCreate() {
        super.onCreate()
        repository = Repository(this)
        gson = Gson()
        dbLinks = DBLinks()
        lastTextMessage = Message()
        conversationIds = ArrayList()
        currentUser = User()
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

        /*LocalBroadcastManager.getInstance(this).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "Current user received")
                currentUser = intent.getSerializableExtra("currentUser") as User
                listenForEventRequest(currentUser._id)
            }
        }, IntentFilter("current-user"))*/

    }

    @SuppressLint("StaticFieldLeak")
    private fun reqCurrentUser(id: String) {
        object : AsyncTask<Void, Void, Int>() {
            override fun doInBackground(vararg p0: Void?): Int? {
                Log.d(TAG, "id -> $id")
                val gson = Gson()
                currentUser = repository.getUserForServerId(id)
                Log.d(TAG, "User from local db -> " + gson.toJson(currentUser))
                return 0
            }

            override fun onPostExecute(result: Int?) {
                super.onPostExecute(result)
                userRetrieved = true
                listenForEventRequest(currentUser._id)
                listenForEventRequestCancel(currentUser._id)
            }
        }.execute()
    }

    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)
        Log.d(TAG, "onMessageReceived has been called")

        if (p0!!.data.isNotEmpty()) {
            val data = p0.data["data"]!!.subSequence(1, p0.data["data"]!!.length - 1)
            Log.d(TAG, "received message data -> $data")

            val message = gson.fromJson(data.toString(), Message::class.java)

            if (message.type == Message.MESSAGE_DUMMY) {
                if (!userRetrieved) {
                    Log.d(TAG, "Called from line 121")
                    reqCurrentUser(message.senderId)
                }
            }

            if (message.type == Message.MESSAGE_TEXT) {
                if (lastTextMessage.senderId.compareTo(message.senderId, false) != 0) {
                    if (!userRetrieved) {
                        Log.d(TAG, "Called from line 129")
                        reqCurrentUser(message.conversationId)
                    }
                    if (message.senderId !in conversationIds) {
                        // A new user is contacting me

                        conversationIds.add(message.senderId)
                        Log.d(TAG, "Conversations updated -> " + conversationIds.size)
                        val userStatus = UserStatus()
                        userStatus.userId = message.senderId
                        repository.insertUserStatus(userStatus)
                        listenForUserStatusChange(message.senderId)
                        Log.d(TAG, "line 140: New conversation with -> " + message.senderId)

                        broadcastNewConversation(message)
                    }
                }
                if (lastTextMessage.senderId.compareTo(message.senderId, false) == 0) {
                    if (!userRetrieved) {
                        Log.d(TAG, "Called from line 137")
                        reqCurrentUser(message.senderId)
                    }
                    if (message.conversationId !in conversationIds) {
                        conversationIds.add(message.conversationId)
                        val userStatus = UserStatus()
                        userStatus.userId = message.conversationId
                        repository.insertUserStatus(userStatus)
                        listenForUserStatusChange(message.conversationId)
                        Log.d(TAG, "line 156: New conversation with -> " + message.conversationId)
                    }
                }

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

    private fun listenForEventRequest(userId: String) {
        Log.d(TAG, "listenForEventRequest: called, id -> $userId")
        socket.on(SocketEvents.permissionRequired(userId)) { args ->
            val data = args[0] as JSONObject
            broadcastEventRequest(JSONUtils.jsonObjToEventRequest(data))
        }
    }

    private fun listenForEventRequestCancel(userId: String) {
        Log.d(TAG, "listenForEventRequestCancel: called, id -> $userId")
        socket.on(SocketEvents.requestCancel(userId)) { args ->
            val data = args[0] as JSONObject
            broadcastEventRequest(JSONUtils.jsonObjToEventRequest(data))
        }
    }

    private fun listenForUserStatusChange(userId: String) {
        Log.d(TAG, "Starting to listen for status change for user with id -> $userId")
        socket.on(SocketEvents.listenForUserStatusChange(userId)) { args ->
            val data = args[0] as JSONObject
            Log.d(TAG, "Status changed for this user")
            Log.d(TAG, "Data -> $data")

            val userStatus = gson.fromJson(data.toString(), UserStatus::class.java) as UserStatus
            userStatus.inConversationWith = data.getString("inConversationWith")
            Log.d(TAG, "In conversation with -> " + userStatus.inConversationWith)
            broadcastStatusChange(userStatus)
            Thread {
                repository.updateUserStatus(userStatus.userId, userStatus.status, userStatus.statusChangedAt, userStatus.inConversationWith)
                if (userStatus.status == UserStatus.IN_CONVERSATION) {
                    val readAt = getCurrentTime()
                    if (userStatus.inConversationWith.compareTo(currentUser._id, false) == 0) {
                        Log.d(TAG, "Updating messages for conversation with id -> $userId")
                        repository.updateUnreadMessagesInConversation(true, readAt, userId)
                    }
                }
            }.start()
        }

    }

    private fun broadcastStatusChange(userStatus: UserStatus) {
        val intent = Intent("status-changed")
        intent.putExtra("user-status", userStatus)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastEventRequest(eventRequest: EventRequest) {
        val intent = Intent("event-request")
        intent.putExtra("event-request", eventRequest)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastNewConversation(message: Message) {
        val conversation = Conversation()
        conversation.lastMessage = message

        val intent = Intent("new-conversation")
        intent.putExtra("conversation", conversation)
        intent.putExtra("from_service", true)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastMessage(message: Message) {
        val intent = Intent("new-message")
        intent.putExtra("message", message)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun getCurrentTime() : String {
        return Calendar.getInstance().time.toString()
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

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }


}