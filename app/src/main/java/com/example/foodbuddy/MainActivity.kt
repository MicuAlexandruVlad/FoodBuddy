package com.example.foodbuddy

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.nfc.Tag
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.transition.Fade
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

        setupExitTransition()

        pager = findViewById(R.id.pager)
        loading = findViewById(R.id.rl_loading)

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

        client.get(dbLinks.usersDiscoverFilter, params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                super.onSuccess(statusCode, headers, response)

                val status = response!!.getInt("status")
                val usersArray = response.getJSONArray("users")
                val usersImagesArray = response.getJSONArray("userImages")
                if (status == HttpStatus.SC_OK) {
                    val userImages = ArrayList<UserImage>()
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
                        usersInSameArea.add(user)
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

    private fun setupExitTransition() {
        val fade = Fade()
        fade.duration = 500
        window.enterTransition = fade
        window.exitTransition = fade
    }


}
