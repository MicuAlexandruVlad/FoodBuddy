package com.example.foodbuddy

import java.io.Serializable

class DiscoverFilter: Serializable {
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
}