package com.example.foodbuddy

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView

class SettingsActivity : AppCompatActivity() {

    private lateinit var body: RelativeLayout
    private lateinit var title: TextView
    private lateinit var nightMode: ImageView
    private lateinit var dayMode: ImageView
    private lateinit var dynamicColors: Switch
    private lateinit var imagePick: RelativeLayout

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getPreferences(Context.MODE_PRIVATE)

        bindViews()

        dynamicColors.setOnCheckedChangeListener { _, b ->

            if (b) {
                imagePick.visibility = View.VISIBLE
                writeToSharedPreferences(R.string.mode, "dynamic")
            }
            else {
                imagePick.visibility = View.GONE
                writeToSharedPreferences(R.string.mode, "standard")
            }
        }

        imagePick.setOnClickListener {

        }

        dayMode.setOnClickListener {
            val jet = ContextCompat.getColor(this, R.color.jet)
            val white = ContextCompat.getColor(this, R.color.white)
            animateBackgroundColorChange(body, jet, white)
            animateTextColorChange(title, white, jet)
            writeToSharedPreferences(R.string.mode, "day")
        }

        nightMode.setOnClickListener {
            val white = ContextCompat.getColor(this, R.color.white)
            val jet = ContextCompat.getColor(this, R.color.jet)
            animateBackgroundColorChange(body, white, jet)
            animateTextColorChange(title, jet, white)
            writeToSharedPreferences(R.string.mode, "night")
        }
    }

    private fun bindViews() {
        body = findViewById(R.id.rl_body)
        title = findViewById(R.id.tv_title)
        nightMode = findViewById(R.id.iv_night)
        dayMode = findViewById(R.id.iv_day)
        dynamicColors = findViewById(R.id.s_dynamic_colors)
        imagePick = findViewById(R.id.rl_image_pick)
    }

    private fun animateBackgroundColorChange(view: View, startColor: Int, endColor: Int) {
        val animator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
        animator.duration = 300
        animator.addUpdateListener {
            view.setBackgroundColor(it.animatedValue as Int)
        }
        animator.start()
    }

    private fun animateTextColorChange(view: TextView, startColor: Int, endColor: Int) {
        val animator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
        animator.duration = 300
        animator.addUpdateListener {
            view.setTextColor(it.animatedValue as Int)
        }
        animator.start()
    }

    private fun writeToSharedPreferences(key: Int, value: String) {
        with (sharedPreferences.edit()) {
            putString(getString(key), value)
            apply()
        }
    }
}
