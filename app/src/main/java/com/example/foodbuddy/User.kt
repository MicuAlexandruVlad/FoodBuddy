package com.example.foodbuddy

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "User")
class User: Serializable {
    @PrimaryKey(autoGenerate = true)
    var localDbId: Long? = null

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
    var online: Boolean = false
    var college: String = ""
    var profileImageId: String = ""
    var deviceToken: String = ""

    @Ignore
    var galleryImageIds = ArrayList<String>()

}