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

    override fun toString(): String {
        return "User(email='$email', password='$password', firstName='$firstName', lastName='$lastName', bio='$bio', gender='$gender', age=$age, city='$city', country='$country', eatTimePeriods='$eatTimePeriods', eatTimes=$eatTimes, birthDate='$birthDate', genderToMeet='$genderToMeet', maxAge=$maxAge, minAge=$minAge, profileSetupComplete=$profileSetupComplete, student=$student)"
    }


}