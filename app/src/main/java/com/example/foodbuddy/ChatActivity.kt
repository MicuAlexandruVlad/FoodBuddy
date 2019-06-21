package com.example.foodbuddy

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem

class ChatActivity : AppCompatActivity() {

    companion object {
        const val SETTINGS_REQ_CODE = 22
    }

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        bindViews()

        toolbar.title = ""

        setSupportActionBar(toolbar)
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.tb_chat_toolbar)
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
