package com.example.foodbuddy

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.transition.Fade
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
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

    companion object {
        const val TAG = "MainActivity"
        const val REQ_PROFILE_EDIT = 41
    }

    private lateinit var pager: ViewPager
    private lateinit var adapter: MainPagerAdapter
    private lateinit var loading: RelativeLayout
    private lateinit var toolbar: Toolbar
    private lateinit var leftIcon: ImageView
    private lateinit var rightIcon: ImageView
    private lateinit var toolbarTitle: TextView

    private var fromProfileSetup = false
    private lateinit var currentUser: User
    private lateinit var usersInSameArea: ArrayList<User>
    private lateinit var userImages: ArrayList<UserImage>
    private lateinit var conversations: ArrayList<Conversation>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(applicationContext)

        setupExitTransition()

        pager = findViewById(R.id.pager)
        loading = findViewById(R.id.rl_loading)
        toolbar = findViewById(R.id.tb_main_toolbar)
        leftIcon = findViewById(R.id.iv_left_icon)
        toolbarTitle = findViewById(R.id.tv_main_toolbar_title)
        rightIcon = findViewById(R.id.iv_right_icon)

        setSupportActionBar(toolbar)

        usersInSameArea = ArrayList()
        userImages = ArrayList()
        conversations = ArrayList()

        currentUser = intent.getSerializableExtra("currentUser") as User
        fromProfileSetup = intent.getBooleanExtra("fromProfileSetup", false)
        conversations = intent.getSerializableExtra("conversations") as ArrayList<Conversation>

        Log.d(TAG, "conversations -> " + conversations.size)


        FirebaseMessaging.getInstance().subscribeToTopic(currentUser._id)
        Log.d(TAG, "subscribed to -> " + currentUser._id)

        leftIcon.setOnClickListener {
            if (pager.currentItem != 0)
                pager.currentItem--
        }

        rightIcon.setOnClickListener {
            if (pager.currentItem != 2)
                pager.currentItem++
        }

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

            }

            @SuppressLint("SetTextI18n")
            override fun onPageSelected(p0: Int) {
                when (p0) {
                    0 -> {
                        // messages fragment selected
                        toolbarTitle.text = "Messages"
                        leftIcon.visibility = View.GONE
                        rightIcon.visibility = View.VISIBLE
                        rightIcon.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.discover))
                    }
                    1 -> {
                        // discover fragment selected
                        toolbarTitle.text = "Discover"
                        leftIcon.visibility = View.VISIBLE
                        rightIcon.visibility = View.VISIBLE
                        leftIcon.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.message))
                        rightIcon.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.event))
                    }
                    2 -> {
                        // events fragment selected
                        toolbarTitle.text = "Events"
                        leftIcon.visibility = View.VISIBLE
                        rightIcon.visibility = View.GONE
                        leftIcon.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.discover))
                    }
                }
            }

        })

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
                if (status == HttpStatus.SC_OK) {
                    val usersArray = response.getJSONArray("users")
                    val usersImagesArray = response.getJSONArray("userImages")
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
                    toolbar.visibility = View.VISIBLE

                    val bundleMessages = Bundle()
                    val bundleDiscover = Bundle()
                    val bundleEvents = Bundle()
                    bundleDiscover.putSerializable("user_images", userImages)
                    bundleDiscover.putSerializable("found_users", usersInSameArea)
                    bundleMessages.putSerializable("conversations", conversations)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.edit_profile -> {
                val intent = Intent(applicationContext, EditUserProfileActivity::class.java)
                intent.putExtra("currentUser", currentUser)
                startActivityForResult(intent, REQ_PROFILE_EDIT)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PROFILE_EDIT && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                currentUser = data.getSerializableExtra("currentUser") as User
            }
        }
    }
}
