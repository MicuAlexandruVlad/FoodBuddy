package com.example.foodbuddy

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.gson.Gson
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.HttpStatus
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MessagesFragment : Fragment() {

    companion object {
        const val TAG = "MessagesFragment"
    }

    private lateinit var bundle: Bundle
    private lateinit var currentUser: User
    private lateinit var conversations: ArrayList<Conversation>
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var repository: Repository

    private lateinit var parentPager: ViewPager
    private lateinit var adapter: ConversationAdapter
    private lateinit var noMessages: RelativeLayout
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments!!
        currentUser = bundle.getSerializable("currentUser") as User
        conversations = bundle.getSerializable("conversations") as ArrayList<Conversation>
        Log.d(TAG, "items -> " + conversations.size)

        val gson = Gson()
        repository = Repository(context!!)

        Log.d(TAG, "conversations -> " + gson.toJson(conversations))

        // TODO: duplicate conversation is added when a person is contacting me
        // also the data for that user is empty, only the message data is stored
        // kinda solved -> it is from onResume in WelcomeActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)

        bindViews(view)

        receiveNewConversation()
        receiveLastMessage()
        receiveLastMessageFromService()
        receiveUserStatusChange()


        if (conversations.size == 0) {
            recyclerView.visibility = View.GONE
            noMessages.visibility = View.VISIBLE
        }
        else {
            recyclerView.visibility = View.VISIBLE
            noMessages.visibility = View.GONE
        }

        // TODO: this should not happen...but it does...
        //  every time i start a conversation with someone, a conversation with the myself is always saved
        for (index in 0 until conversations.size)
            if (conversations[index].conversationId.compareTo(currentUser._id, false) == 0) {
                conversations.removeAt(index)
                break
            }


        adapter = ConversationAdapter(conversations, context, currentUser)
        layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager

        reqNumUnreadMessages()

        return view
    }

    @SuppressLint("StaticFieldLeak")
    private fun reqNumUnreadMessages() {
        for (index in 0 until conversations.size) {
            val conversation = conversations[index]
            object : AsyncTask<Void, Void, Int>() {
                override fun doInBackground(vararg p0: Void?): Int {
                    return repository.getUnreadMessagesForConversation(false, conversation.conversationId).size
                }

                override fun onPostExecute(unreadMessages: Int?) {
                    super.onPostExecute(unreadMessages)
                    Log.d(TAG, "Unread messages for conversation with user " + conversation.conversationUser.lastName +
                            " -> $unreadMessages")

                    conversation.unreadMessages = unreadMessages as Int
                    adapter.notifyDataSetChanged()

                }
            }.execute()
        }
    }

    private fun bindViews(view: View) {
        parentPager = activity!!.findViewById(R.id.pager)
        recyclerView = view.findViewById(R.id.rv_conversations)
        noMessages = view.findViewById(R.id.rl_no_messages)
    }

    private fun receiveNewConversation() {
        LocalBroadcastManager.getInstance(activity!!.applicationContext).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val fromService = intent.getBooleanExtra("from_service", false)

                if (!fromService) {
                    val conversation = intent.getSerializableExtra("conversation") as Conversation
                    Log.d(TAG, "Conversation not from service")
                    Log.d(TAG, "new conversation with -> " + conversation.conversationUser.firstName)

                    conversations.add(conversation)
                    // TODO: Image is not displayed when a new conversation is added
                    Log.d(TAG, "conversation id -> " + conversation.conversationId)
                    Log.d(TAG, "conversation image -> " + conversation.profilePhotoId)

                    activity!!.runOnUiThread {
                        adapter.notifyItemInserted(conversations.size - 1)
                        if (noMessages.visibility == View.VISIBLE) {
                            noMessages.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                        }
                    }
                }
                else {
                    val conversation = intent.getSerializableExtra("conversation") as Conversation

                    Log.d(TAG, "Conversation from service")
                    resolveConversation(conversation)
                }
            }
        }, IntentFilter("new-conversation"))
    }

    private fun resolveConversation(conversation: Conversation) {
        val lastMessage = conversation.lastMessage
        val conversationUserId = lastMessage.senderId

        val client = AsyncHttpClient()
        val params = RequestParams()
        val dbLinks = DBLinks()

        params.put("ids", conversationUserId)

        client.get(dbLinks.getUserById, params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                super.onSuccess(statusCode, headers, response)

                val status = response!!.getInt("status")

                if (status == HttpStatus.SC_OK) {
                    val userData = response.getJSONArray("users").getJSONObject(0)
                    val conversationUser = User()
                    conversationUser._id = userData.getString("_id")
                    conversationUser.firstName = userData.getString("firstName")
                    conversationUser.lastName = userData.getString("lastName")
                    conversationUser.bio = userData.getString("bio")
                    conversationUser.gender = userData.getString("gender")
                    conversationUser.age = userData.getInt("age")
                    conversationUser.city = userData.getString("city")
                    conversationUser.country = userData.getString("country")
                    conversationUser.eatTimePeriods = userData.getString("eatTimePeriods")
                    conversationUser.eatTimes = userData.getInt("eatTimes")
                    conversationUser.birthDate = userData.getString("birthDate")
                    conversationUser.genderToMeet = userData.getString("genderToMeet")
                    conversationUser.maxAge = userData.getInt("maxAge")
                    conversationUser.minAge = userData.getInt("minAge")
                    conversationUser.profileSetupComplete = userData.getBoolean("profileSetupComplete")
                    conversationUser.student = userData.getBoolean("student")
                    conversationUser.zodiac = userData.getString("zodiac")
                    conversationUser.college = userData.getString("college")

                    conversation.conversationUser = conversationUser
                    Log.d(TAG, "Contacted by user with id -> " + conversationUser._id)


                    client.get(dbLinks.smallProfileImagesById, params, object : JsonHttpResponseHandler() {
                        override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                            super.onSuccess(statusCode, headers, response)

                            val imageStatus = response!!.getInt("status")

                            if (imageStatus == HttpStatus.SC_OK) {
                                val imageData = response.getJSONArray("userImages").getJSONObject(0)

                                conversation.profilePhotoId = imageData.getString("_id")

                                Log.d(TAG, "conversation image -> " + conversation.profilePhotoId)

                                activity!!.runOnUiThread {
                                    // TODO: for some reason the id of the user is not saved
                                    conversations.add(conversation)
                                    if (noMessages.visibility == View.VISIBLE) {
                                        noMessages.visibility = View.GONE
                                        recyclerView.visibility = View.VISIBLE
                                    }
                                    adapter.notifyItemInserted(conversations.size - 1)
                                }
                            }
                        }
                    })
                }
            }
        })
    }

    private fun receiveLastMessage() {
        LocalBroadcastManager.getInstance(activity!!.applicationContext).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val lastMessage = intent.getSerializableExtra("message") as Message
                val conversationId = intent.getStringExtra("conversationId")

                for (index in 0 until conversations.size) {
                    if (conversations[index].conversationId.compareTo(conversationId, false) == 0) {
                        conversations[index].lastMessage = lastMessage
                        adapter.notifyItemChanged(index)
                        break
                    }
                }
            }
        }, IntentFilter("last-message"))
    }

    private fun receiveLastMessageFromService() {
        LocalBroadcastManager.getInstance(activity!!.applicationContext).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val lastMessage = intent.getSerializableExtra("message") as Message

                for (index in 0 until conversations.size) {
                    if (conversations[index].conversationId.compareTo(lastMessage.senderId, false) == 0) {
                        conversations[index].lastMessage = lastMessage
                        conversations[index].unreadMessages++
                        Log.d(TAG, "Unread messages -> " + conversations[index].unreadMessages)
                        adapter.notifyItemChanged(index)
                        break
                    }
                }
            }
        }, IntentFilter("new-message"))
    }

    private fun receiveUserStatusChange() {
        LocalBroadcastManager.getInstance(activity!!.applicationContext).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val userStatus = intent.getSerializableExtra("user-status") as UserStatus
                for (index in 0 until conversations.size) {
                    val conversation = conversations[index]
                    if (conversation.conversationId.compareTo(userStatus.userId, false) == 0) {
                        conversation.lastMessage.read = true
                        adapter.notifyItemChanged(index)
                        break
                    }
                }
            }
        }, IntentFilter("status-changed"))
    }

    override fun onResume() {
        super.onResume()

        reqNumUnreadMessages()
    }
}