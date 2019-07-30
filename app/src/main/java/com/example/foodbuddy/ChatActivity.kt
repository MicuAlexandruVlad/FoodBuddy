package com.example.foodbuddy

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.widget.*
import com.bumptech.glide.Glide
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import java.net.URISyntaxException
import java.util.*
import kotlin.collections.ArrayList
import android.util.TypedValue
import android.view.*
import com.google.gson.Gson
import org.json.JSONObject


class ChatActivity : AppCompatActivity() {

    companion object {
        const val SETTINGS_REQ_CODE = 22
        const val TAG = "ChatActivity"
    }

    private lateinit var currentUser: User
    private lateinit var foundUser: User
    private var userStatus = UserStatus()
    private lateinit var dbLinks: DBLinks
    private lateinit var messages: ArrayList<Message>
    private lateinit var repository: Repository
    private lateinit var messagesAdapter: MessageAdapter
    private lateinit var socket: Socket
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var conversation: Conversation
    private lateinit var serverMethods: ServerMethods

    private lateinit var toolbar: Toolbar
    private lateinit var back: ImageView
    private lateinit var isOnline: TextView
    private lateinit var toolbarProfileImage: ImageView
    private lateinit var toolbarTitle: TextView
    private lateinit var noMessages: RelativeLayout
    private lateinit var messageField: EditText
    private lateinit var openCamera: ImageView
    private lateinit var sendMessage: ImageView
    private lateinit var messagesRv: RecyclerView
    private lateinit var body: ScrollView

    private var isKeyboardOpen = false
    private var canDisplayNotification = true
    private var fromConversationAdapter = false

    private var lastSeenMessageIndex = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        currentUser = intent.getSerializableExtra("currentUser") as User
        currentUser.online = true
        conversation = Conversation()
        serverMethods = ServerMethods(this)

        fromConversationAdapter = intent.getBooleanExtra("fromConversationAdapter", false)
        if (fromConversationAdapter) {
            conversation = intent.getSerializableExtra("conversation") as Conversation
            foundUser = conversation.conversationUser
            foundUser.profileImageId = conversation.profilePhotoId
        }
        else {
            foundUser = intent.getSerializableExtra("foundUser") as User
        }
        dbLinks = DBLinks()
        messages = ArrayList()
        repository = Repository(this)

        canDisplayNotification = false

        Log.d(TAG, "chatting with -> " + foundUser._id)

        bindViews()

        try {
            socket = IO.socket(dbLinks.socketLink)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        socket.connect()
        emitUserInConversation(currentUser, foundUser)

        layoutManager = LinearLayoutManager(this@ChatActivity, LinearLayout.VERTICAL, false)

        reqMessages()
        reqFoundUserStatus()

        receiveMessage()
        receiveUserStatusChange()
        setNewMessagesAsRead()

        toolbar.title = ""
        toolbarTitle.text = foundUser.firstName + " " + foundUser.lastName
        Glide.with(this).load(dbLinks.getImageSmall(foundUser._id, foundUser.profileImageId)).into(toolbarProfileImage)

        setSupportActionBar(toolbar)
        broadcastNotificationFlag()


        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
                val itemPosition = p0.adapterPosition
                deleteMessage(messages[itemPosition].id!!)
                messages.removeAt(itemPosition)
                messagesAdapter.notifyItemRemoved(itemPosition)
            }
        }
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(messagesRv)

        back.setOnClickListener { finish() }

        // detect keyboard open
        body.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = body.rootView.height - body.height
            if (heightDiff > dpToPx(this, 200f) && !isKeyboardOpen) {
                layoutManager.scrollToPositionWithOffset(messages.size - 1, 0)
                isKeyboardOpen = true
            }
            else if (heightDiff < dpToPx(this, 200f))
                isKeyboardOpen = false
            Log.d(TAG, "heightDiff -> $heightDiff")
        }

        sendMessage.setOnClickListener {
            val messageText = messageField.text.toString()
            if (messageText.compareTo("") == 0)
                Toast.makeText(this, "Message field can not be empty", Toast.LENGTH_SHORT).show()
            else {
                val message = Message()
                message.messageText = messageText
                message.senderId = currentUser._id
                message.senderName = currentUser.firstName + " " + currentUser.lastName
                message.timestamp = getCurrentTime()
                message.type = Message.MESSAGE_TEXT
                message.conversationId = foundUser._id

                if (messages.size == 0)
                    broadcastNewConversation(message)

                val obj = JSONObject()
                messageToJson(message, obj)
                socket.emit("chat", obj)

                if (userStatus != null) {
                    Log.d(TAG, "Conversation user status -> " + userStatus.status)
                    Log.d(TAG, "In conversation with -> " + userStatus.inConversationWith)

                    if (userStatus.status == UserStatus.IN_CONVERSATION) {
                        if (userStatus.inConversationWith.compareTo(currentUser._id, false) == 0) {
                            message.read = true
                            message.readAt = getCurrentTime()
                        }
                    }
                }

                broadcastMessageInserted(message)

                repository.insertMessage(message)
                messageField.setText("")
                if (messages.size == 0)
                    noMessages.visibility = View.GONE
                else
                    broadcastLastMessage(message)
                messages.add(message)
                messagesAdapter.notifyItemInserted(messages.size - 1)

                layoutManager.scrollToPositionWithOffset(messages.size - 1, 0)

            }
        }
    }

    private fun setNewMessagesAsRead() {
        Thread {
            repository.updateUnreadMessagesInConversation(true, getCurrentTime(), conversation.conversationId)
        }.start()
    }

    @SuppressLint("StaticFieldLeak")
    private fun reqFoundUserStatus() {
        object : AsyncTask<Void, Void, Int>() {
            override fun doInBackground(vararg p0: Void?): Int? {
                userStatus = repository.getUserStatusForId(foundUser._id)
                return 0
            }

            @SuppressLint("SetTextI18n")
            override fun onPostExecute(result: Int?) {
                super.onPostExecute(result)
                if (userStatus != null) {
                    val status = userStatus.status
                    runOnUiThread {
                        isOnline.visibility = View.VISIBLE
                        when (status) {
                            0 -> isOnline.text = "offline"
                            1, 2 -> isOnline.text = "online"
                        }
                    }
                }
            }
        }.execute()
    }


    private fun broadcastMessageInserted(message: Message) {
        val intent = Intent("inserted-message")
        intent.putExtra("message", message)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastLastMessage(message: Message) {
        val intent = Intent("last-message")
        intent.putExtra("message", message)
        intent.putExtra("conversationId", conversation.conversationId)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastNewConversation(message: Message) {
        val conversation = Conversation()
        conversation.conversationUser = foundUser
        conversation.conversationId = foundUser._id
        conversation.lastMessage = message
        conversation.profilePhotoId = foundUser.profileImageId

        val intent = Intent("new-conversation")
        intent.putExtra("conversation", conversation)
        intent.putExtra("from_service", false)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun receiveMessage() {
        LocalBroadcastManager.getInstance(this).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val message = intent.getSerializableExtra("message") as Message

                if (message.senderId.compareTo(currentUser._id, false) != 0) {
                    if (message.senderId.compareTo(foundUser._id, false) == 0) {
                        runOnUiThread {
                            if (messages.size == 0) {
                                noMessages.visibility = View.GONE
                                messagesRv.visibility = View.VISIBLE
                            }
                            message.read = true
                            message.readAt = getCurrentTime()
                            messages.add(message)

                            setNewMessagesAsRead()

                            messagesAdapter.notifyItemInserted(messages.size - 1)
                            layoutManager.scrollToPositionWithOffset(messages.size - 1, 0)
                        }
                    }
                }
            }
        }, IntentFilter("new-message"))
    }

    private fun initLastSeenIndex() {
        if (messages.size > 0) {
            for (index in messages.size - 1 until 0) {
                if (!messages[index].read) {
                    lastSeenMessageIndex = index
                    Log.d(TAG, "Index value -> $lastSeenMessageIndex")
                    break
                }
            }
        }
        else
            lastSeenMessageIndex = 0
    }

    private fun receiveUserStatusChange() {
        LocalBroadcastManager.getInstance(this).registerReceiver(object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            override fun onReceive(context: Context, intent: Intent) {
                userStatus = intent.getSerializableExtra("user-status") as UserStatus


                if (userStatus.userId.compareTo(foundUser._id, false) == 0) {
                    runOnUiThread {
                        val status = userStatus.status
                        if (status in arrayOf(UserStatus.ONLINE, UserStatus.IN_CONVERSATION)) {
                            isOnline.text = "online"
                        }
                        else {
                            isOnline.text = "offline"
                        }

                        if (status == UserStatus.IN_CONVERSATION) {
                            if (userStatus.inConversationWith.compareTo(currentUser._id, false) == 0) {
                                updateReadMessagesUi()
                            }
                            else
                                initLastSeenIndex()
                        }
                    }
                }
            }
        }, IntentFilter("status-changed"))
    }

    private fun updateReadMessagesUi() {
        for (index in lastSeenMessageIndex until messages.size) {
            if (messages[index].senderId.compareTo(currentUser._id, false) == 0 && !messages[index].read) {
                messages[index].read = true
                messages[index].readAt = getCurrentTime()
            }
        }
        runOnUiThread {
            messagesAdapter.notifyDataSetChanged()
            layoutManager.scrollToPositionWithOffset(messages.size - 1, 0)
        }
    }

    private fun messageToJson(message: Message, obj: JSONObject) {
        obj.put("conversationId", message.conversationId)
        obj.put("imageData", message.imageData)
        obj.put("imagePath", message.imagePath)
        obj.put("messageText", message.messageText)
        obj.put("seen", message.seen)
        obj.put("seenAt", message.seenAt)
        obj.put("senderId", message.senderId)
        obj.put("senderName", message.senderName)
        obj.put("timestamp", message.timestamp)
        obj.put("type", message.type)
        obj.put("voiceData", message.voiceData)
        obj.put("voicePath", message.voicePath)
    }

    @SuppressLint("StaticFieldLeak")
    private fun reqMessages() {
        object : AsyncTask<Void, Void, Int>() {
            override fun doInBackground(vararg p0: Void?): Int? {
                messages.addAll(repository.getMessagesForConversation(foundUser._id))
                Log.d(TAG, "messages found -> " + messages.size)
                val gson = Gson()
                for (index in 0 until messages.size) {
                    Log.d(TAG, "Message json -> " + gson.toJson(messages[index]))
                }
                return 0
            }

            override fun onPostExecute(result: Int?) {
                super.onPostExecute(result)
                initLastSeenIndex()
                runOnUiThread {
                    if (messages.size != 0)
                        noMessages.visibility = View.GONE
                    else
                        noMessages.visibility = View.VISIBLE
                    messagesAdapter = MessageAdapter(messages, this@ChatActivity, currentUser, foundUser)
                    messagesRv.adapter = messagesAdapter
                    messagesRv.layoutManager = layoutManager
                    layoutManager.scrollToPositionWithOffset(messages.size - 1, 0)
                }
            }
        }.execute()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.tb_chat_toolbar)
        back = findViewById(R.id.iv_back)
        isOnline = findViewById(R.id.tv_is_online)
        toolbarProfileImage = findViewById(R.id.iv_toolbar_profile_image)
        toolbarTitle = findViewById(R.id.tv_toolbar_user_name)
        noMessages = findViewById(R.id.rl_first_time)
        messageField = findViewById(R.id.et_message)
        openCamera = findViewById(R.id.iv_open_camera)
        sendMessage = findViewById(R.id.iv_send_message)
        messagesRv = findViewById(R.id.rv_messages)
        body = findViewById(R.id.sv_body)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.settings -> {
                val intent = Intent(applicationContext, SettingsActivity::class.java)
                startActivityForResult(intent, SETTINGS_REQ_CODE)
                true
            }
            R.id.create_event -> {
                showEventCreationDialog(socket)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showEventCreationDialog(socket: Socket) {
        val dialog = EventCreationRequestPermissionDialog(this, socket)
        dialog.currentUser = currentUser
        dialog.foundUser = foundUser
        dialog.create()
        dialog.show()
    }

    private fun getCurrentTime() : String {
        return Calendar.getInstance().time.toString()
    }

    private fun dpToPx(context: Context, valueInDp: Float): Float {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
    }

    @SuppressLint("StaticFieldLeak")
    private fun deleteMessage(id: Long) {
        object : AsyncTask<Void, Void, Int>() {
            override fun doInBackground(vararg p0: Void?): Int? {
                repository.deleteMessageById(id)
                return 0
            }
        }.execute()
    }

    private fun broadcastNotificationFlag() {
        val intent = Intent("display-notification")
        intent.putExtra("notify", canDisplayNotification)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onResume() {
        super.onResume()
        canDisplayNotification = false
        broadcastNotificationFlag()
    }

    override fun onPause() {
        super.onPause()
        canDisplayNotification = true
        broadcastNotificationFlag()
    }

    override fun onDestroy() {
        super.onDestroy()
        canDisplayNotification = true
        broadcastNotificationFlag()
        emitUserOutOfConversation(currentUser)
        //socket.disconnect()
    }

    private fun emitUserInConversation(currentUser: User, foundUser: User) {
        val userStatus = UserStatus()
        val gson = Gson()
        userStatus.inConversationWith = foundUser._id
        userStatus.userId = currentUser._id
        userStatus.status = UserStatus.IN_CONVERSATION
        userStatus.statusChangedAt = getCurrentTime()
        val data = JSONObject(gson.toJson(userStatus))
        socket.emit(SocketEvents.STATUS_CHANGE, data)
    }

    private fun emitUserOutOfConversation(currentUser: User) {
        val userStatus = UserStatus()
        val gson = Gson()
        userStatus.inConversationWith = ""
        userStatus.userId = currentUser._id
        userStatus.status = UserStatus.ONLINE
        userStatus.statusChangedAt = getCurrentTime()
        val data = JSONObject(gson.toJson(userStatus))
        socket.emit(SocketEvents.STATUS_CHANGE, data)
    }



}
