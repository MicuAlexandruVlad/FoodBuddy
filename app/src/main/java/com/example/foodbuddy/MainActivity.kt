package com.example.foodbuddy

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager

class MainActivity : AppCompatActivity() {

    private lateinit var pager: ViewPager
    private lateinit var adapter: MainPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pager = findViewById(R.id.pager)

        supportActionBar!!.hide()

        val user = intent.getSerializableExtra("currentUser") as User

        val bundleMessages = Bundle()
        val bundleDiscover = Bundle()
        val bundleEvents = Bundle()
        adapter = MainPagerAdapter(supportFragmentManager, bundleMessages, bundleDiscover, bundleEvents, user)

        pager.adapter = adapter

    }
}
