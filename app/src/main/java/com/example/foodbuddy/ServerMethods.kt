package com.example.foodbuddy

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.HttpStatus
import org.json.JSONObject

class ServerMethods(private val context: Context) {
    private val dbLinks = DBLinks()

    // status => 0 - offline, 1 - online
    /*fun updateUserStatus(user: User, status: Int) {
        val client = AsyncHttpClient()
        val params = RequestParams()

        params.put("userId", user._id)
        params.put("status", status)

        client.post(dbLinks.updateUserStatus, params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                super.onSuccess(statusCode, headers, response)

                val responseStatus = response!!.getInt("status")
                if (responseStatus == HttpStatus.SC_OK) {
                    Log.d(ChatActivity.TAG, "from ServerMethods::updateUserStatus -> data updated")
                }
                else if (responseStatus == HttpStatus.SC_NOT_FOUND) {
                    Toast.makeText(context, "User entry not found. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }*/
}