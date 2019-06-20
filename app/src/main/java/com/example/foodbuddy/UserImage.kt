package com.example.foodbuddy

import java.io.Serializable

class UserImage: Serializable {
    var userId = ""
    var imageName = ""
    var isProfileImage = false
    var normalProfileImageData = ""
    var smallProfileImageData = ""
    var galleryImageData = ""

}