package com.example.foodbuddy

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide

class PreUserDetailsActivity : AppCompatActivity() {

    private lateinit var currentUser: User
    private lateinit var foundUser: User
    private lateinit var dbLinks: DBLinks

    private lateinit var profileImage: ImageView
    private lateinit var userName: TextView
    private lateinit var sendMessage: RelativeLayout
    private lateinit var location: TextView
    private lateinit var bio: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_user_details)

        foundUser = intent.getSerializableExtra("user") as User
        currentUser = intent.getSerializableExtra("currentUser") as User
        dbLinks = DBLinks()

        bindViews()

        userName.text = foundUser.firstName + " " + foundUser.lastName
        bio.text = foundUser.bio
        location.text = foundUser.city + ", " + foundUser.country

        Glide.with(this).load(dbLinks.getImageNormal(foundUser._id, foundUser.profileImageId)).into(profileImage)

        sendMessage.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("currentUser", currentUser)
            intent.putExtra("foundUser", foundUser)
            startActivity(intent)
        }
    }

    private fun bindViews() {
        profileImage = findViewById(R.id.iv_profile_image)
        userName = findViewById(R.id.tv_user_name)
        sendMessage = findViewById(R.id.rl_send_message)
        location = findViewById(R.id.tv_location)
        bio = findViewById(R.id.tv_user_bio)
    }
}
