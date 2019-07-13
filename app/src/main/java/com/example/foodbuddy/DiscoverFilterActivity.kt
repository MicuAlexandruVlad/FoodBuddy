package com.example.foodbuddy

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.transition.Fade
import android.util.Log
import android.view.View
import android.widget.*
import com.rengwuxian.materialedittext.MaterialEditText
import com.xw.repo.BubbleSeekBar
import android.widget.TimePicker
import android.app.TimePickerDialog
import java.util.*
import kotlin.collections.ArrayList


class DiscoverFilterActivity : AppCompatActivity() {

    companion object {
        const val TAG = "DiscoverFilterActivity"
    }

    private lateinit var adapter: ZodiacSignAdapter
    private lateinit var zodiacItems: ArrayList<ZodiacSign>
    private lateinit var filter: DiscoverFilter

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
    private lateinit var city: MaterialEditText
    private lateinit var country: MaterialEditText
    private lateinit var addZodiacSigns: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var student: CheckBox
    private lateinit var collegeName: MaterialEditText
    private lateinit var startTime: MaterialEditText
    private lateinit var endTime: MaterialEditText
    private lateinit var toleranceTv: TextView
    private lateinit var tolerance: BubbleSeekBar
    private lateinit var toleranceHelp: ImageView

    private var canReset = true

    @SuppressLint("RestrictedApi", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_filter)

        setupEnterTransition()

        filter = intent.getSerializableExtra("userFilter") as DiscoverFilter
        zodiacItems = ArrayList()
        bindViews()

        adapter = ZodiacSignAdapter(zodiacItems, this, true)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        zodiacItems.addAll(filter.zodiacSigns)
        adapter.notifyDataSetChanged()

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
                canReset = true
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
                canReset = true
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

        addZodiacSigns.setOnClickListener {
            val dialog = PickZodiacSignDialog(this)
            dialog.create()
            dialog.show()
            dialog.setOnDismissListener {
                zodiacItems.clear()
                zodiacItems.addAll(dialog.getSelectedItems())
                canReset = true

                Log.d(TAG, "items in list -> " + zodiacItems.size)
                adapter.notifyDataSetChanged()
            }
        }

        student.setOnCheckedChangeListener { _, b ->
            if (b) {
                collegeName.visibility = View.VISIBLE
                canReset = true
            }
            else {
                collegeName.visibility = View.GONE
                canReset = true
            }
        }

        startTime.setOnClickListener {
            val hour = startTime.text!!.split(":")[0].toInt()
            val minute = startTime.text!!.split(":")[1].toInt()
            val timePicker: TimePickerDialog
            timePicker = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    if (selectedMinute < 10 && selectedHour > 9)
                        startTime.setText("$selectedHour:0$selectedMinute")
                    else if (selectedMinute > 9 && selectedHour < 10)
                        startTime.setText("0$selectedHour:$selectedMinute")
                    else if (selectedMinute < 10 && selectedHour < 10)
                        startTime.setText("0$selectedHour:0$selectedMinute")
                    else
                        startTime.setText("$selectedHour:$selectedMinute")
                    canReset = true
                }, hour, minute, true
            )
            timePicker.setTitle("Select Time")
            timePicker.show()
        }

        endTime.setOnClickListener {
            val hour = endTime.text!!.split(":")[0].toInt()
            val minute = endTime.text!!.split(":")[1].toInt()
            val timePicker: TimePickerDialog
            timePicker = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    if (selectedMinute < 10 && selectedHour > 9)
                        endTime.setText("$selectedHour:0$selectedMinute")
                    else if (selectedMinute > 9 && selectedHour < 10)
                        endTime.setText("0$selectedHour:$selectedMinute")
                    else if (selectedMinute < 10 && selectedHour < 10)
                        endTime.setText("0$selectedHour:0$selectedMinute")
                    else
                        endTime.setText("$selectedHour:$selectedMinute")
                    canReset = true
                }, hour, minute, true
            )
            timePicker.setTitle("Select Time")
            timePicker.show()
        }

        toleranceHelp.setOnClickListener {

        }

        tolerance.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {

            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
                toleranceTv.text = progress.toString()
                canReset = true
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

        cancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        reset.setOnClickListener {
            setImplicitFilterValues()
        }

        apply.setOnClickListener {
            val cityName = city.text.toString()
            val countryName = country.text.toString()
            var collegeNameVal = ""
            val startTimeVal = startTime.text.toString()
            val endTimeVal = endTime.text.toString()
            val genderToMeet: String = if (male.elevation > 0)
                "Male"
            else
                "Female"

            if (student.isChecked) {
                collegeNameVal = collegeName.text.toString()
            }

            if (cityName.compareTo("") == 0 || countryName.compareTo("") == 0)
                Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show()
            else if (startTimeVal.compareTo("") == 0 || endTimeVal.compareTo("") == 0)
                Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show()
            else {
                filter.gender = genderToMeet
                filter.isStudent = student.isChecked
                filter.collegeName = collegeNameVal
                filter.minAge = minAgeTv.text.toString().toInt()
                filter.maxAge = maxAgeTv.text.toString().toInt()
                filter.tolerance = toleranceTv.text.toString().toInt()
                filter.eatTimes = "$startTimeVal - $endTimeVal"
                filter.zodiacSigns.clear()
                filter.zodiacSigns.addAll(zodiacItems)
                intent.putExtra("userFilter", filter)
                intent.putExtra("filterChanged", canReset)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun setImplicitFilterValues() {
        val genderToMeet = filter.gender
        val min = filter.minAge
        val max = filter.maxAge
        val prefCity = filter.city
        val prefCountry = filter.country

        if (country.text.toString().compareTo(filter.country) != 0)
            canReset = true
        if (city.text.toString().compareTo(filter.city) != 0)
            canReset = true
        if (collegeName.text.toString().compareTo(filter.collegeName) != 0)
            canReset = true

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
            toleranceTv.text = filter.tolerance.toString()
            tolerance.setProgress(filter.tolerance.toFloat())
            collegeName.setText(filter.collegeName)
            student.isChecked = filter.isStudent
            if (student.isChecked)
                collegeName.visibility = View.VISIBLE
            else
                collegeName.visibility = View.GONE
            Log.d(TAG, "user eat times -> " + filter.eatTimes)
            for (index in 0 until filter.eatTimes.split("/").size) {
                val entry = filter.eatTimes.split("/")[index]
                startTime.setText(entry.split(" - ")[0])
                Log.d(TAG, "eat time -> $entry")
                endTime.setText(entry.split(" - ")[1])
                break
            }
            city.setText(prefCity)
            country.setText(prefCountry)
            zodiacItems.clear()
            zodiacItems.addAll(filter.zodiacSigns)
            adapter.notifyDataSetChanged()
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
        student = findViewById(R.id.cb_student)
        collegeName = findViewById(R.id.met_college)
        startTime = findViewById(R.id.met_start_time)
        endTime = findViewById(R.id.met_end_time)
        toleranceTv = findViewById(R.id.tv_tolerance)
        tolerance = findViewById(R.id.bsb_tolerance)
        toleranceHelp = findViewById(R.id.iv_tolerance_info)
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
