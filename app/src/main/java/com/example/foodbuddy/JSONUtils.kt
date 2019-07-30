package com.example.foodbuddy

import android.util.Log
import org.json.JSONObject

class JSONUtils {
    companion object {
        const val TAG = "JSONUtils"

        fun jsonObjToEventRequest(jsonObject: JSONObject): EventRequest {
            val eventRequest = EventRequest()
            Log.d(TAG, "jsonObjToEventRequest: $jsonObject")
            eventRequest.permUserId = jsonObject.getString("perm_user_id")
            eventRequest.reqUserId = jsonObject.getString("req_user_id")
            eventRequest.reqType = jsonObject.getInt("action")

            return eventRequest
        }
    }
}