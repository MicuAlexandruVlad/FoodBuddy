package com.example.foodbuddy

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.transition.Fade
import android.transition.Slide
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.HttpStatus
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

class WelcomeActivity : AppCompatActivity() {

    val TAG = "WelcomeActivity"
    val dbLinks = DBLinks()
    val REGISTER_REQ_CODE = 141

    private lateinit var currentUser: User
    private lateinit var client: AsyncHttpClient

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var foodLogo: TextView
    private lateinit var buddyLogo: TextView
    private lateinit var animationView: LottieAnimationView
    private lateinit var logo: ImageView
    private lateinit var introHolder: RelativeLayout
    private lateinit var afterIntro: RelativeLayout
    private lateinit var  login: Button
    private lateinit var register: TextView
    private lateinit var load: LottieAnimationView


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        supportActionBar?.hide()

        foodLogo = findViewById(R.id.tv_food)
        buddyLogo = findViewById(R.id.tv_buddy)
        animationView = findViewById(R.id.l_loading)
        logo = findViewById(R.id.iv_logo)
        introHolder = findViewById(R.id.rl_intro_holder)
        afterIntro = findViewById(R.id.rl_after_anim_holder)
        login = findViewById(R.id.btn_login)
        register = findViewById(R.id.tv_register)
        email = findViewById(R.id.et_email)
        password = findViewById(R.id.et_password)
        load = findViewById(R.id.l_success)

        introHolder.visibility = View.VISIBLE
        animationView.visibility = View.GONE
        foodLogo.visibility = View.GONE
        buddyLogo.visibility = View.GONE
        load.visibility = View.INVISIBLE

        currentUser = User()

        email.setText("t@t.com")
        password.setText("testpass123")

        register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivityForResult(intent, REGISTER_REQ_CODE)
        }

        login.setOnClickListener {
            val emailVal = email.text.toString()
            val passVal = password.text.toString()

            load.visibility = View.VISIBLE
            load.playAnimation()


            if (emailVal.compareTo("", false) == 0 || passVal.compareTo("", false) == 0) {
                Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show()
            }
            else {
                client = AsyncHttpClient()
                val params = RequestParams()

                currentUser.email = emailVal
                currentUser.password = passVal

                params.put("email", currentUser.email)
                params.put("password", currentUser.password)
                client.post(dbLinks.loginWithEmail, params, object: JsonHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                        super.onSuccess(statusCode, headers, response)

                        val status = response!!.getInt("status")

                        if (status == HttpStatus.SC_OK) {
                            val userData = response.getJSONArray("user")!!.getJSONObject(0)
                            currentUser.student = userData.getBoolean("student")
                            currentUser.profileSetupComplete = userData.getBoolean("profileSetupComplete")
                            currentUser.minAge = userData.getInt("minAge")
                            currentUser.maxAge = userData.getInt("maxAge")
                            currentUser.genderToMeet = userData.getString("genderToMeet")
                            currentUser.gender = userData.getString("gender")
                            currentUser.birthDate = userData.getString("birthDate")
                            currentUser.eatTimes = userData.getInt("eatTimes")
                            currentUser.eatTimePeriods = userData.getString("eatTimePeriods")
                            currentUser.country = userData.getString("country")
                            currentUser.city = userData.getString("city")
                            currentUser.lastName = userData.getString("lastName")
                            currentUser.firstName = userData.getString("firstName")
                            currentUser._id = userData.getString("_id")
                            currentUser.age = userData.getInt("age")
                            currentUser.bio = userData.getString("bio")
                            currentUser.zodiac = userData.getString("zodiac")
                            currentUser.college = userData.getString("college")

                            runOnUiThread {
                                load.cancelAnimation()
                                load.visibility = View.INVISIBLE
                            }

                            if (currentUser.profileSetupComplete) {
                                val intent = Intent(applicationContext, MainActivity::class.java)
                                intent.putExtra("currentUser", currentUser)
                                intent.putExtra("fromProfileSetup", false)
                                startActivity(intent)
                            }
                            else {
                                val intent = Intent(applicationContext, ProfileSetupActivity::class.java)
                                intent.putExtra("currentUser", currentUser)
                                startActivity(intent)
                            }
                        }
                        else if (status == HttpStatus.SC_NOT_FOUND){
                            Toast.makeText(this@WelcomeActivity, "User not found", Toast.LENGTH_SHORT).show()

                            runOnUiThread {
                                load.cancelAnimation()
                                load.visibility = View.INVISIBLE
                            }
                        }
                    }

                    override fun onFailure(
                        statusCode: Int,
                        headers: Array<out Header>?,
                        throwable: Throwable?,
                        errorResponse: JSONObject?
                    ) {
                        super.onFailure(statusCode, headers, throwable, errorResponse)
                    }
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REGISTER_REQ_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val userId = data.getStringExtra("userId")
                val passwordVal = data.getStringExtra("password")
                val emailVal = data.getStringExtra("email")
                currentUser._id = userId
                currentUser.password = passwordVal
                currentUser.email = emailVal

                email.setText(emailVal)
                password.setText(passwordVal)
            }
        }
    }
}
