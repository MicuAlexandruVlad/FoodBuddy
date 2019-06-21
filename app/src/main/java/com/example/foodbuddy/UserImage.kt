package com.example.foodbuddy

import java.io.Serializable

class UserImage: Serializable {
    var id = ""
    var userId = ""
    var imageName = ""
    var isProfileImage = false
    var normalProfileImageData = ""
    var smallProfileImageData = ""
}