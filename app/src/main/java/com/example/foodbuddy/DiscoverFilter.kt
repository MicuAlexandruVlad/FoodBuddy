package com.example.foodbuddy

import android.annotation.SuppressLint
import android.util.Log
import org.apache.commons.lang3.time.DateUtils
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DiscoverFilter: Serializable {

    companion object {
        const val TAG = "DiscoverFilter"
    }

    var gender: String = ""
    var minAge: Int = 18
    var maxAge: Int = 80
    var city: String = ""
    var country: String = ""
    var zodiacSigns: ArrayList<ZodiacSign> = ArrayList()
    var isStudent: Boolean = false
    var collegeName: String = ""
    var eatTimes: String = ""
    var tolerance:Int = 0


    fun filterByTime(users: ArrayList<User>): ArrayList<User> {
        val filteredUsers = ArrayList<User>()
        Log.d(TAG, "User eat times -> $eatTimes")
        var currentUserStartTimeStr = eatTimes.split(" - ")[0]
        Log.d(TAG, "current user start time before tolerance -> $currentUserStartTimeStr")
        currentUserStartTimeStr = addTolerance(currentUserStartTimeStr, -tolerance)
        var currentUserEndTimeStr = eatTimes.split(" - ")[1]
        Log.d(TAG, "current user end time before tolerance -> $currentUserEndTimeStr")
        currentUserEndTimeStr = addTolerance(currentUserEndTimeStr, tolerance)
        val currentUserStartTime = currentUserStartTimeStr.split(":")[0].toDouble() * 100.00 +
                currentUserStartTimeStr.split(":")[1].toDouble()
        val currentUserEndTime = currentUserEndTimeStr.split(":")[0].toDouble() * 100.00 +
                currentUserEndTimeStr.split(":")[1].toDouble()
        Log.d(TAG, "current user start time post tolerance -> $currentUserStartTime")
        Log.d(TAG, "current user end time post tolerance -> $currentUserEndTime")
        for (index in 0 until users.size) {
            val user = users[index]
            val eatTimePeriods = user.eatTimePeriods.split("/")
            for (i in 0 until eatTimePeriods.size) {
                val startTimeStr = eatTimePeriods[i].split(" - ")[0]
                val endTimeStr = eatTimePeriods[i].split(" - ")[1]
                val startTime = startTimeStr.split(":")[0].toDouble() * 100.00 +
                        startTimeStr.split(":")[1].toDouble()
                val endTime = endTimeStr.split(":")[0].toDouble() * 100.00 +
                        endTimeStr.split(":")[1].toDouble()
                Log.d(TAG, "found user start time -> $startTime")
                Log.d(TAG, "found user end time -> $endTime")
                if (currentUserStartTime == startTime) {
                    filteredUsers.add(user)
                    break
                }
                if (currentUserStartTime > startTime && currentUserEndTime < endTime) {
                    filteredUsers.add(user)
                    break
                }
                if (currentUserStartTime < startTime && currentUserEndTime > startTime) {
                    filteredUsers.add(user)
                    break
                }
                if (currentUserStartTime < startTime && currentUserEndTime > endTime) {
                    filteredUsers.add(user)
                    break
                }
                if (currentUserStartTime < endTime && currentUserEndTime > endTime) {
                    filteredUsers.add(user)
                    break
                }
            }
        }
        return filteredUsers
    }

    @SuppressLint("SimpleDateFormat")
    private fun addTolerance(time: String, tolerance: Int): String {
        val format = SimpleDateFormat("HH:mm")
        var date = format.parse(time)
        date = DateUtils.addMinutes(date, tolerance)
        Log.d(TAG, "Calendar time -> " + date.toString().split(" ")[3].split(":")[0] +
            ":" + date.toString().split(" ")[3].split(":")[1])
        return date.toString().split(" ")[3].split(":")[0] +
                ":" + date.toString().split(" ")[3].split(":")[1]
    }
}