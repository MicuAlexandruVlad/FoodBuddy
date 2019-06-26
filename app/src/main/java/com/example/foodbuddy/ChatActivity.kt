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
import org.json.JSONObject


class ChatActivity : AppCompatActivity() {

    companion object {
        const val SETTINGS_REQ_CODE = 22
        const val TAG = "ChatActivity"
    }

    private lateinit var currentUser: User
    private lateinit var foundUser: User
    private lateinit var dbLinks: DBLinks
    private lateinit var messages: ArrayList<Message>
    private lateinit var repository: Repository
    private lateinit var messagesAdapter: MessageAdapter
    private lateinit var socket: Socket
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarProfileImage: ImageView
    private lateinit var toolbarTitle: TextView
    private lateinit var noMessages: RelativeLayout
    private lateinit var messageField: EditText
    private lateinit var openCamera: ImageView
    private lateinit var sendMessage: ImageView
    private lateinit var messagesRv: RecyclerView
    private lateinit var body: ScrollView

    private var isKeyboardOpen = false
    private var canDisplayNotification = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        currentUser = intent.getSerializableExtra("currentUser") as User
        foundUser = intent.getSerializableExtra("foundUser") as User
        dbLinks = DBLinks()
        messages = ArrayList()
        repository = Repository(this)

        canDisplayNotification = false

        bindViews()

        try {
            socket = IO.socket(dbLinks.socketLink)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        socket.connect()

        layoutManager = LinearLayoutManager(this@ChatActivity, LinearLayout.VERTICAL, false)

        reqMessages()

        receiveMessage()

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

                val obj = JSONObject()
                messageToJson(message, obj)
                socket.emit("chat", obj)

                repository.insertMessage(message)
                messageField.setText("")
                if (messages.size == 0)
                    noMessages.visibility = View.GONE
                messages.add(message)
                messagesAdapter.notifyItemInserted(messages.size - 1)

                layoutManager.scrollToPositionWithOffset(messages.size - 1, 0)
            }
        }
    }

    private fun receiveMessage() {
        LocalBroadcastManager.getInstance(this).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val message = intent.getSerializableExtra("message") as Message

                messages.add(message)

                runOnUiThread {
                    messagesAdapter.notifyItemInserted(messages.size - 1)
                    layoutManager.scrollToPositionWithOffset(messages.size - 1, 0)
                }
            }
        }, IntentFilter("new-message"))
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
                return 0
            }

            override fun onPostExecute(result: Int?) {
                super.onPostExecute(result)
                runOnUiThread {
                    if (messages.size != 0)
                        noMessages.visibility = View.GONE
                    else
                        noMessages.visibility = View.VISIBLE
                    messagesAdapter = MessageAdapter(messages, this@ChatActivity, currentUser)
                    messagesRv.adapter = messagesAdapter
                    messagesRv.layoutManager = layoutManager
                    layoutManager.scrollToPositionWithOffset(messages.size - 1, 0)
                }
            }
        }.execute()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.tb_chat_toolbar)
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
            else -> super.onOptionsItemSelected(item)
        }
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
    }
}
