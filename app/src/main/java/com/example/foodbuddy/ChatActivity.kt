package com.example.foodbuddy

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import java.util.*
import kotlin.collections.ArrayList

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

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarProfileImage: ImageView
    private lateinit var toolbarTitle: TextView
    private lateinit var noMessages: RelativeLayout
    private lateinit var messageField: EditText
    private lateinit var openCamera: ImageView
    private lateinit var sendMessage: ImageView
    private lateinit var messagesRv: RecyclerView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        currentUser = intent.getSerializableExtra("currentUser") as User
        foundUser = intent.getSerializableExtra("foundUser") as User
        dbLinks = DBLinks()
        messages = ArrayList()
        repository = Repository(this)

        bindViews()

        reqMessages()

        toolbar.title = ""
        toolbarTitle.text = foundUser.firstName + " " + foundUser.lastName
        Glide.with(this).load(dbLinks.getImageSmall(foundUser._id, foundUser.profileImageId)).into(toolbarProfileImage)

        setSupportActionBar(toolbar)

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
                message.conversationId = currentUser._id + foundUser._id

                repository.insertMessage(message)
                messageField.setText("")
                if (messages.size == 0)
                    noMessages.visibility = View.GONE
                messages.add(message)
                messagesAdapter.notifyItemInserted(messages.size - 1)
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun reqMessages() {
        object : AsyncTask<Void, Void, Int>() {
            override fun doInBackground(vararg p0: Void?): Int? {
                messages.addAll(repository.getMessagesForConversation(currentUser._id + foundUser._id))
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
                    messagesRv.layoutManager = LinearLayoutManager(this@ChatActivity,
                        LinearLayout.VERTICAL, false)
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
}
