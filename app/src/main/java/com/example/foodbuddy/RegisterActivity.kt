package com.example.foodbuddy

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.HttpStatus
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {
    val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.hide()

        val parentIntent = intent

        val register = findViewById<Button>(R.id.btn_register)
        val email = findViewById<EditText>(R.id.et_email)
        val password = findViewById<EditText>(R.id.et_password)
        val repeatPassword = findViewById<EditText>(R.id.et_repeat_password)

        register.setOnClickListener {
            val emailVal = email.text.toString()
            val passVal = password.text.toString()
            val repeatPassVal = repeatPassword.text.toString()

            if (emailVal.compareTo("") == 0 || passVal.compareTo("") == 0 ||
                repeatPassVal.compareTo("") == 0) {
                Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show()
            }
            else if (passVal.compareTo(repeatPassVal, false) != 0) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
            else {
                val dbLinks = DBLinks()
                val user = User()
                user.email = emailVal
                user.password = passVal

                Log.d(TAG, "register new user -> $user")

                val client = AsyncHttpClient()
                val params = RequestParams()
                params.put("email", emailVal)
                params.put("password", user.password)
                params.put("firstName", user.firstName)
                params.put("lastName", user.lastName)
                params.put("bio", user.bio)
                params.put("gender", user.gender)
                params.put("age", user.age)
                params.put("city", user.city)
                params.put("country", user.country)
                params.put("eatTimePeriods", user.eatTimePeriods)
                params.put("eatTimes", user.eatTimes)
                params.put("birthDate", user.birthDate)
                params.put("genderToMeet", user.genderToMeet)
                params.put("maxAge", user.maxAge)
                params.put("minAge", user.minAge)
                params.put("profileSetupComplete", user.profileSetupComplete)
                params.put("student", user.student)
                params.put("zodiac", user.zodiac)
                params.put("college", user.college)
                params.put("deviceToken", user.deviceToken)

                client.post(dbLinks.registerUserEmail, params, object: JsonHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                        super.onSuccess(statusCode, headers, response)

                        val status = response!!.getInt("status")

                        if (status == HttpStatus.SC_CREATED) {
                            val userId = response.getString("id")
                            Toast.makeText(applicationContext, "User registered", Toast.LENGTH_SHORT).show()
                            parentIntent.putExtra("userId", userId)
                            parentIntent.putExtra("email", emailVal)
                            parentIntent.putExtra("password", passVal)
                            setResult(Activity.RESULT_OK, parentIntent)
                            finish()
                        }
                        else if (status == HttpStatus.SC_CONFLICT) {
                            Toast.makeText(applicationContext, "User already exists", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?,
                                           errorResponse: JSONObject?
                    ) {
                        super.onFailure(statusCode, headers, throwable, errorResponse)
                        Log.d(TAG, "Error retrieving data from server: -> " + errorResponse.toString())
                    }
                })
            }
        }
    }
}
