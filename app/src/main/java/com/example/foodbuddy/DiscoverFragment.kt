package com.example.foodbuddy

import android.app.Activity
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
import android.widget.Toast
import com.google.gson.Gson
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.HttpStatus
import org.json.JSONObject
import java.lang.StringBuilder

class DiscoverFragment : Fragment() {

    companion object {
        const val FILTER_REQ_CODE = 14
        const val TAG = "DiscoverFragment"
    }

    private val t = "DiscoverFragment"

    private lateinit var bundle: Bundle
    private lateinit var currentUser: User
    private lateinit var userFilter: DiscoverFilter
    private var filterChanged: Boolean = false
    private lateinit var dbLinks: DBLinks

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
        dbLinks = DBLinks()
        userFilter = DiscoverFilter()
        userFilter.city = currentUser.city
        userFilter.country = currentUser.country
        userFilter.minAge = currentUser.minAge
        userFilter.maxAge = currentUser.maxAge
        userFilter.gender = currentUser.genderToMeet
        userFilter.eatTimes = currentUser.eatTimePeriods
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

            }
        })

        filter.setOnClickListener {
            val intent = Intent(context, DiscoverFilterActivity::class.java)
            intent.putExtra("userFilter", userFilter)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILTER_REQ_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {

                userFilter = data.getSerializableExtra("userFilter") as DiscoverFilter
                filterChanged = data.getBooleanExtra("filterChanged", false)
                Toast.makeText(context, "filter changed -> $filterChanged", Toast.LENGTH_SHORT).show()

                var url = ""
                if (!userFilter.isStudent && userFilter.zodiacSigns.size > 0) {
                    url = dbLinks.zodiacOnlyFilter
                }
                else if (userFilter.isStudent && userFilter.zodiacSigns.size == 0 && userFilter.collegeName.compareTo("") != 0) {
                    url = dbLinks.studentCollegeFilter
                }
                else if (userFilter.isStudent && userFilter.zodiacSigns.size == 0 && userFilter.collegeName.compareTo("") == 0) {
                    url = dbLinks.studentOnlyFilter
                }
                else if (userFilter.isStudent && userFilter.zodiacSigns.size > 0 && userFilter.collegeName.compareTo("") == 0) {
                    url = dbLinks.studentZodiacFilter
                }
                else if (!userFilter.isStudent && userFilter.zodiacSigns.size == 0) {
                    url = dbLinks.usersDiscoverFilter
                }
                else {
                    url = dbLinks.fullFilter
                }
                reqUsers(userFilter, url)
            }
        }
    }

    private fun reqUsers(filter: DiscoverFilter, url: String) {
        val client = AsyncHttpClient()
        val params = RequestParams()

        params.put("userCountry", filter.country)
        params.put("userCity", filter.city)
        params.put("userPreferredGender", filter.gender)
        params.put("userMinAge", filter.minAge)
        params.put("userMaxAge", filter.maxAge)
        if (filter.isStudent)
            params.put("isStudent", "true")
        else
            params.put("isStudent", "true^false")
        params.put("collegeName", filter.maxAge)
        val builder = StringBuilder()
        for (index in 0 until filter.zodiacSigns.size) {
            val zodiacSign = filter.zodiacSigns[index]
            builder.append(zodiacSign.zodiacName)
            if (index < filter.zodiacSigns.size - 1)
                builder.append("^")
        }
        params.put("zodiac", builder.toString())
        params.put("limit", 50)

        client.get(url, params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                super.onSuccess(statusCode, headers, response)

                val status = response!!.getInt("status")

                if (status == HttpStatus.SC_OK) {
                    val usersArray = response.getJSONArray("users")
                    val usersImagesArray = response.getJSONArray("userImages")
                    val userImages = ArrayList<UserImage>()
                    foundUsers.clear()
                    for (index in 0 until usersImagesArray.length()) {
                        val obj = usersImagesArray.getJSONObject(index)
                        val userImage = UserImage()
                        userImage.id = obj.getString("_id")
                        userImage.userId = obj.getString("userId")
                        userImage.isProfileImage = obj.getBoolean("isProfileImage")
                        userImages.add(userImage)
                    }
                    for (index in 0 until usersArray.length()) {
                        val gson = Gson()
                        val obj = usersArray.getJSONObject(index)
                        val user = gson.fromJson(obj.toString(), User::class.java) as User
                        for (i in 0 until userImages.size) {
                            val image = userImages[i]
                            if (user._id == image.userId) {
                                if (image.isProfileImage) {
                                    user.profileImageId = image.id
                                }
                                else {
                                    user.galleryImageIds.add(image.id)
                                }
                            }
                        }
                        foundUsers.add(user)
                    }
                    Log.d(TAG, "Found users -> " + foundUsers.size)
                    (context as Activity).runOnUiThread {
                        adapter.notifyDataSetChanged()
                    }
                }
                else {
                    foundUsers.clear()
                    (context as Activity).runOnUiThread {
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }
}