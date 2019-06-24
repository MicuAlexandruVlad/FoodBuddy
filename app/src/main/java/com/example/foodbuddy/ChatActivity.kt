package com.example.foodbuddy

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class ChatActivity : AppCompatActivity() {

    companion object {
        const val SETTINGS_REQ_CODE = 22
    }

    private lateinit var currentUser: User
    private lateinit var foundUser: User
    private lateinit var dbLinks: DBLinks

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarProfileImage: ImageView
    private lateinit var toolbarTitle: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        currentUser = intent.getSerializableExtra("currentUser") as User
        foundUser = intent.getSerializableExtra("foundUser") as User
        dbLinks = DBLinks()

        bindViews()

        toolbar.title = ""
        toolbarTitle.text = foundUser.firstName + " " + foundUser.lastName
        Glide.with(this).load(dbLinks.getImageSmall(foundUser._id, foundUser.profileImageId)).into(toolbarProfileImage)

        setSupportActionBar(toolbar)
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.tb_chat_toolbar)
        toolbarProfileImage = findViewById(R.id.iv_toolbar_profile_image)
        toolbarTitle = findViewById(R.id.tv_toolbar_user_name)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.settings -> {
                val intent = Intent(applicationContext, SettingsActivity::class.java)
                startActivityForResult(intent, SETTINGS_REQ_CODE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
