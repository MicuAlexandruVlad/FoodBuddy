package com.example.foodbuddy

import android.app.Dialog
import android.content.Context
import android.os.Bundle

class PermissionReqDialog(c: Context): Dialog(c) {
    private lateinit var reqUser: User
    private lateinit var permUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}