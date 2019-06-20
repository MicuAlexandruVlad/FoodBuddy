package com.example.foodbuddy

class DBLinks {
    private val baseLink: String = "http://192.168.0.19:3000/"
    val registerUserEmail: String = baseLink + "users/register-user-email/"
    val loginWithEmail: String = baseLink + "users/login-email/"
    val updateUserData: String = baseLink + "users/update-user-data/"
    val uploadUserImage: String = baseLink + "user-images/upload-user-image/"
}