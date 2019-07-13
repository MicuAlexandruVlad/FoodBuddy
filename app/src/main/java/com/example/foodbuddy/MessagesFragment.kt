package com.example.foodbuddy

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import kotlinx.android.synthetic.main.activity_main.*

class MessagesFragment : Fragment() {

    companion object {
        const val TAG = "MessagesFragment"
    }

    private lateinit var bundle: Bundle
    private lateinit var currentUser: User
    private lateinit var conversations: ArrayList<Conversation>
    private lateinit var layoutManager: LinearLayoutManager

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)

        bindViews(view)

        receiveNewConversation()
        receiveLastMessage()

        if (conversations.size == 0) {
            recyclerView.visibility = View.GONE
            noMessages.visibility = View.VISIBLE
        }
        else {
            recyclerView.visibility = View.VISIBLE
            noMessages.visibility = View.GONE
        }

        adapter = ConversationAdapter(conversations, context, currentUser)
        layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager

        return view
    }

    private fun bindViews(view: View) {
        parentPager = activity!!.findViewById(R.id.pager)
        recyclerView = view.findViewById(R.id.rv_conversations)
        noMessages = view.findViewById(R.id.rl_no_messages)
    }

    private fun receiveNewConversation() {
        LocalBroadcastManager.getInstance(activity!!.applicationContext).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val conversation = intent.getSerializableExtra("conversation") as Conversation
                Log.d(TAG, "new conversation with -> " + conversation.conversationUser.firstName)

                conversations.add(conversation)
                // TODO: Image is not displayed when a new conversation is added

                (getContext() as Activity).runOnUiThread {
                    adapter.notifyItemInserted(conversations.size - 1)
                }
            }
        }, IntentFilter("new-conversation"))
    }

    private fun receiveLastMessage() {
        LocalBroadcastManager.getInstance(activity!!.applicationContext).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val lastMessage = intent.getSerializableExtra("message") as Message
                val conversationId = intent.getStringExtra("conversationId")

                for (index in 0 until conversations.size) {
                    if (conversations[index].conversationId.compareTo(conversationId, false) == 0) {
                        conversations[index].lastMessage = lastMessage
                        (getContext() as Activity).runOnUiThread {
                            adapter.notifyItemChanged(index)
                        }
                        break
                    }
                }
            }
        }, IntentFilter("last-message"))
    }
}