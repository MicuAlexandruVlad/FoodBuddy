package com.example.foodbuddy

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide

class UserDetailsActivity : AppCompatActivity() {

    companion object {
        const val TAG = "UserDetailsActivity"
    }

    private lateinit var currentUser: User
    private lateinit var foundUser: User
    private lateinit var dbLinks: DBLinks

    private lateinit var topProfileImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        foundUser = intent.getSerializableExtra("user") as User
        currentUser = intent.getSerializableExtra("currentUser") as User
        dbLinks = DBLinks()

        bindViews()

        Glide.with(this).load(dbLinks.getImageNormal(foundUser._id, foundUser.profileImageId))
            .into(topProfileImage)
    }

    private fun bindViews() {
        topProfileImage = findViewById(R.id.iv_big_profile_image)
    }
}
