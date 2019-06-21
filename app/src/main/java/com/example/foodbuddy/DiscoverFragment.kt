package com.example.foodbuddy

import android.content.Intent
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
import android.widget.RelativeLayout

class DiscoverFragment : Fragment() {

    companion object {
        const val FILTER_REQ_CODE = 14
    }

    private val t = "DiscoverFragment"

    private lateinit var bundle: Bundle
    private lateinit var currentUser: User

    private lateinit var parentPager:ViewPager
    private lateinit var messages: ImageView
    private lateinit var events: ImageView
    private lateinit var filter: RelativeLayout
    private lateinit var header: RelativeLayout
    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: DiscoverAdapter
    private lateinit var foundUsers: ArrayList<User>
    private lateinit var userImages: ArrayList<UserImage>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments!!
        currentUser = bundle.getSerializable("currentUser") as User
        foundUsers = bundle.getSerializable("found_users") as ArrayList<User>
        userImages = bundle.getSerializable("user_images") as ArrayList<UserImage>
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_discover, container, false)

        bindViews(view)

        adapter = DiscoverAdapter(foundUsers, context, currentUser)
        adapter.userImages = userImages
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.isNestedScrollingEnabled = false

        messages.setOnClickListener {
            parentPager.currentItem = 0
        }

        events.setOnClickListener {
            parentPager.currentItem = 2
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                    filter.visibility = View.GONE
                else
                    filter.visibility = View.VISIBLE
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                Log.d(t, "state -> $newState")
            }
        })

        filter.setOnClickListener {
            val intent = Intent(context, DiscoverFilterActivity::class.java)
            intent.putExtra("currentUser", currentUser)
            startActivityForResult(intent, FILTER_REQ_CODE)
        }

        return view
    }

    private fun bindViews(view: View) {
        parentPager = activity!!.findViewById(R.id.pager)
        messages = view.findViewById(R.id.iv_messages)
        events = view.findViewById(R.id.iv_events)
        filter = view.findViewById(R.id.rl_filter)
        header = view.findViewById(R.id.rl_discover_header)
        recyclerView = view.findViewById(R.id.rv_discover)
    }
}