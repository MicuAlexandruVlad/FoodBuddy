package com.example.foodbuddy

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.media.Image
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.transition.Fade
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import com.rengwuxian.materialedittext.MaterialEditText
import com.xw.repo.BubbleSeekBar

class DiscoverFilterActivity : AppCompatActivity() {

    companion object {
        const val TAG = "DiscoverFilterActivity"
    }

    private lateinit var currentUser: User
    private lateinit var adapter: ZodiacSignAdapter
    private lateinit var zodiacItems: ArrayList<ZodiacSign>

    private lateinit var scrollView: ScrollView
    private lateinit var cancel: FloatingActionButton
    private lateinit var reset: FloatingActionButton
    private lateinit var apply: FloatingActionButton
    private lateinit var male: RelativeLayout
    private lateinit var female: RelativeLayout
    private lateinit var minAge: BubbleSeekBar
    private lateinit var maxAge: BubbleSeekBar
    private lateinit var minAgeTv: TextView
    private lateinit var maxAgeTv: TextView
    private lateinit var city: TextView
    private lateinit var country: TextView
    private lateinit var addZodiacSigns: ImageView
    private lateinit var recyclerView: RecyclerView

    private var canReset = true

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_filter)

        setupEnterTransition()

        currentUser = intent.getSerializableExtra("currentUser") as User
        zodiacItems = ArrayList()

        bindViews()
        setImplicitFilterValues()

        male.setOnClickListener {
            if (male.elevation == 0f) {
                animateElevation(male, 0f, 16f)
                animateElevation(female, 16f, 0f)
            }
            canReset = true
        }

        female.setOnClickListener {
            if (female.elevation == 0f) {
                animateElevation(female, 0f, 16f)
                animateElevation(male, 16f, 0f)
            }
            canReset = true
        }

        minAge.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {

            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
                minAgeTv.text = progress.toString()
            }

            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {

            }

            override fun getProgressOnFinally(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {

            }
        }

        maxAge.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {

            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
                maxAgeTv.text = progress.toString()
            }

            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {

            }

            override fun getProgressOnFinally(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {

            }
        }

        adapter = ZodiacSignAdapter(zodiacItems, this, true)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        addZodiacSigns.setOnClickListener {
            val dialog = PickZodiacSignDialog(this)
            dialog.create()
            dialog.show()
            dialog.setOnDismissListener {
                zodiacItems.clear()
                zodiacItems.addAll(dialog.getSelectedItems())
                Log.d(TAG, "items in list -> " + zodiacItems.size)
                adapter.notifyDataSetChanged()
            }
        }

        cancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        reset.setOnClickListener {
            setImplicitFilterValues()
        }
    }

    private fun setImplicitFilterValues() {
        val genderToMeet = currentUser.genderToMeet
        val min = currentUser.minAge
        val max = currentUser.maxAge
        val prefCity = currentUser.city
        val prefCountry = currentUser.country

        if (canReset) {
            if (genderToMeet.compareTo("male", true) == 0) {
                male.elevation = 16f
                female.elevation = 0f
            }
            else {
                female.elevation = 16f
                male.elevation = 0f
            }
            minAgeTv.text = min.toString()
            maxAgeTv.text = max.toString()
            minAge.setProgress(min.toFloat())
            maxAge.setProgress(max.toFloat())
            city.text = prefCity
            country.text = prefCountry

        }

        canReset = false
    }

    private fun bindViews() {
        scrollView = findViewById(R.id.sv_parent)
        cancel = findViewById(R.id.fab_cancel_filters)
        reset = findViewById(R.id.fab_reset_filters)
        apply = findViewById(R.id.fab_apply_filters)
        male = findViewById(R.id.rl_male)
        female = findViewById(R.id.rl_female)
        minAge = findViewById(R.id.bsb_min_age)
        maxAge = findViewById(R.id.bsb_max_age)
        minAgeTv = findViewById(R.id.tv_min_age)
        maxAgeTv = findViewById(R.id.tv_max_age)
        city = findViewById(R.id.met_city)
        country = findViewById(R.id.met_country)
        addZodiacSigns = findViewById(R.id.iv_add_zodiac_signs)
        recyclerView = findViewById(R.id.rv_zodiac_signs)
    }

    private fun setupEnterTransition() {
        val fade = Fade()
        fade.duration = 500
        window.enterTransition = fade
    }

    private fun animateElevation(relativeLayout: RelativeLayout, start: Float, end: Float) {
        val animator = ObjectAnimator.ofFloat(relativeLayout, "elevation", start, end)
        animator.duration = 300
        animator.start()
    }
}
