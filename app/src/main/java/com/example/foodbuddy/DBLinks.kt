package com.example.foodbuddy

class DBLinks {
    private val baseLink: String = "http://192.168.0.19:3000/"
    val registerUserEmail: String = baseLink + "users/register-user-email/"
    val loginWithEmail: String = baseLink + "users/login-email/"
    val updateUserData: String = baseLink + "users/update-user-data/"
    val uploadUserImage: String = baseLink + "user-images/upload-user-image/"
    val usersDiscoverFilter: String = baseLink + "users/users-discover-filter/"
    val fullFilter: String = baseLink + "users/full-filter/"
    val zodiacOnlyFilter: String = baseLink + "users/zodiac-only-filter/"
    val studentOnlyFilter: String = baseLink + "users/student-only-no-college-filter/"
    val studentCollegeFilter: String = baseLink + "users/student-college-filter/"
    val studentZodiacFilter: String = baseLink + "users/student-zodiac-no-college-filter/"


    fun getImageSmall(userId: String, imageId: String): String {
        return baseLink + "user-images/profile-small/$userId/images/$imageId/"
    }

    fun getImageNormal(userId: String, imageId: String): String {
        return baseLink + "user-images/profile-normal/$userId/images/$imageId/"
    }

}