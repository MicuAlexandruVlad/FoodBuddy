package com.example.foodbuddy

import java.io.Serializable

class User: Serializable {
    var _id: String = ""
    var zodiac: String = ""
    var email: String = ""
    var password: String = ""
    var firstName: String = ""
    var lastName: String = ""
    var bio: String = ""
    var gender: String = ""
    var age: Int = 0
    var city: String = ""
    var country: String = ""
    var eatTimePeriods: String = ""
    var eatTimes: Int = 0
    var birthDate: String = ""
    var genderToMeet: String = ""
    var maxAge: Int = 0
    var minAge: Int = 0
    var profileSetupComplete: Boolean = false
    var student: Boolean = false
    var college: String = ""
    var profileImageId: String = ""
    var deviceToken: String = ""
    var galleryImageIds = ArrayList<String>()

}