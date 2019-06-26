package com.example.foodbuddy

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MessagesFragment : Fragment() {

    companion object {
        const val TAG = "MessagesFragment"
    }

    private lateinit var bundle: Bundle
    private lateinit var currentUser: User
    private lateinit var conversations: ArrayList<Conversation>
    private lateinit var parentPager: ViewPager
    private lateinit var adapter: ConversationAdapter

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

        adapter = ConversationAdapter(conversations, context, currentUser)
        val layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager

        return view
    }

    private fun bindViews(view: View) {
        parentPager = activity!!.findViewById(R.id.pager)
        recyclerView = view.findViewById(R.id.rv_conversations)
    }
}