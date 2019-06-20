package com.example.foodbuddy

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.nfc.Tag
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import com.google.gson.Gson
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.HttpStatus
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var pager: ViewPager
    private lateinit var adapter: MainPagerAdapter
    private lateinit var loading: RelativeLayout

    private val TAG = "MainActivity"

    private var fromProfileSetup = false
    private lateinit var currentUser: User
    private lateinit var usersInSameArea: ArrayList<User>
    private lateinit var userImages: ArrayList<UserImage>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pager = findViewById(R.id.pager)
        loading = findViewById(R.id.rl_loading)

        supportActionBar!!.hide()

        usersInSameArea = ArrayList()
        userImages = ArrayList()

        currentUser = intent.getSerializableExtra("currentUser") as User
        fromProfileSetup = intent.getBooleanExtra("fromProfileSetup", false)

        val currentUserCountry = currentUser.country
        val currentUserCity = currentUser.city
        val preferredGender = currentUser.genderToMeet
        val minAge = currentUser.minAge
        val maxAge = currentUser.maxAge

        reqUsersInSameArea(currentUserCountry, currentUserCity, preferredGender, minAge, maxAge)
    }

    private fun initAdapter(bundleMessages: Bundle, bundleDiscover: Bundle, bundleEvents: Bundle, user: User) {
        adapter = MainPagerAdapter(supportFragmentManager, bundleMessages, bundleDiscover, bundleEvents, user)
        pager.adapter = adapter
    }

    private fun reqUsersInSameArea(currentUserCountry: String, currentUserCity: String, preferredGender: String,
                                   minAge: Int, maxAge: Int) {
        val client = AsyncHttpClient()
        val dbLinks = DBLinks()
        val params = RequestParams()

        params.put("userCountry", currentUserCountry)
        params.put("userCity", currentUserCity)
        params.put("userPreferredGender", preferredGender)
        params.put("userMinAge", minAge)
        params.put("userMaxAge", maxAge)
        params.put("limit", 50)

        Log.d(TAG, "userCountry -> $currentUserCountry")
        Log.d(TAG, "userCity -> $currentUserCity")
        Log.d(TAG, "userPreferredGender -> $preferredGender")
        Log.d(TAG, "userMinAge -> $minAge")
        Log.d(TAG, "userMaxAge -> $maxAge")

        client.get(dbLinks.usersDiscover5, params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                super.onSuccess(statusCode, headers, response)

                val status = response!!.getInt("status")
                if (status == HttpStatus.SC_OK) {
                    val users = response.getJSONArray("users")

                    for (index in 0 until users.length()) {
                        var user: User
                        val obj = users.getJSONObject(index)
                        val gson = Gson()

                        user = gson.fromJson(obj.toString(), User::class.java) as User
                        usersInSameArea.add(user)

                        val images = response.getJSONArray("userImages")
                        var userImage = gson.fromJson(images.getJSONObject(index).toString(), UserImage::class.java) as UserImage
                        userImage.id = images.getJSONObject(index).getString("_id")
                        userImages.add(userImage)
                    }

                    Log.d(TAG, "Users found -> " + usersInSameArea.size)
                }
                if (status == HttpStatus.SC_NO_CONTENT) {
                    Log.d(TAG, "No users found")
                }

                runOnUiThread {
                    rl_loading.visibility = View.GONE
                    pager.visibility = View.VISIBLE

                    val bundleMessages = Bundle()
                    val bundleDiscover = Bundle()
                    val bundleEvents = Bundle()
                    bundleDiscover.putSerializable("user_images", userImages)
                    bundleDiscover.putSerializable("found_users", usersInSameArea)
                    initAdapter(bundleMessages, bundleDiscover, bundleEvents, currentUser)
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                throwable: Throwable?,
                errorResponse: JSONArray?
            ) {
                super.onFailure(statusCode, headers, throwable, errorResponse)
            }
        })
    }


}
