package com.example.foodbuddy

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.HttpStatus
import org.json.HTTP
import org.json.JSONObject
import java.util.*

class EditUserProfileActivity : AppCompatActivity() {
    companion object {
        const val TAG = "EditUserProfileActivity"
    }

    private lateinit var currentUser: User
    private val dbLinks = DBLinks()

    private lateinit var changeProfilePicture: RelativeLayout
    private lateinit var back: ImageView
    private lateinit var done: Button
    private lateinit var profileImage: ImageView
    private lateinit var firstName: RelativeLayout
    private lateinit var firstNameTv: TextView
    private lateinit var editFirstNameRl: RelativeLayout
    private lateinit var editFirstNameEt: EditText
    private lateinit var firstNameDone: FloatingActionButton
    private lateinit var lastName: RelativeLayout
    private lateinit var lastNameTv: TextView
    private lateinit var editLastNameRl: RelativeLayout
    private lateinit var editLastNameEt: EditText
    private lateinit var lastNameDone: FloatingActionButton
    private lateinit var gender: RelativeLayout
    private lateinit var genderTv: TextView
    private lateinit var editGenderRl: RelativeLayout
    private lateinit var male: ImageView
    private lateinit var female: ImageView
    private lateinit var bio: RelativeLayout
    private lateinit var bioTv: TextView
    private lateinit var editBioRl: RelativeLayout
    private lateinit var editBioEt: EditText
    private lateinit var bioDone: FloatingActionButton
    private lateinit var city: RelativeLayout
    private lateinit var cityTv: TextView
    private lateinit var editCityRl: RelativeLayout
    private lateinit var editCityEt: EditText
    private lateinit var cityDone: FloatingActionButton
    private lateinit var country: RelativeLayout
    private lateinit var countryTv: TextView
    private lateinit var editCountryRl: RelativeLayout
    private lateinit var editCountryEt: EditText
    private lateinit var countryDone: FloatingActionButton
    private lateinit var genderToMeet: RelativeLayout
    private lateinit var genderToMeetTv: TextView
    private lateinit var editGenderToMeetRl: RelativeLayout
    private lateinit var genderToMeetMale: ImageView
    private lateinit var genderToMeetFemale: ImageView
    private lateinit var minAge: RelativeLayout
    private lateinit var minAgeTv: TextView
    private lateinit var editMinAgeRl: RelativeLayout
    private lateinit var editMinAgeEt: EditText
    private lateinit var minAgeDone: FloatingActionButton
    private lateinit var maxAge: RelativeLayout
    private lateinit var maxAgeTv: TextView
    private lateinit var editMaxAgeRl: RelativeLayout
    private lateinit var editMaxAgeEt: EditText
    private lateinit var maxAgeDone: FloatingActionButton
    private lateinit var student: CheckBox
    private lateinit var college: RelativeLayout
    private lateinit var collegeTv: TextView
    private lateinit var editCollegeRl: RelativeLayout
    private lateinit var editCollegeEt: EditText
    private lateinit var collegeDone: FloatingActionButton
    private lateinit var zodiac: RelativeLayout
    private lateinit var zodiacTv: TextView
    private lateinit var editZodiacRl: RelativeLayout
    private lateinit var editZodiacEt: EditText
    private lateinit var zodiacDone: FloatingActionButton
    private lateinit var birthDate: RelativeLayout
    private lateinit var birthDateTv: TextView
    private lateinit var editBirthDateRl: RelativeLayout
    private lateinit var editBirthDateEt: EditText
    private lateinit var birthDateDone: FloatingActionButton

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user_profile)

        currentUser = intent.getSerializableExtra("currentUser") as User

        bindViews()

        reqProfilePhoto(currentUser._id)

        firstNameTv.text = currentUser.firstName
        lastNameTv.text = currentUser.lastName
        genderTv.text = currentUser.gender
        bioTv.text = currentUser.bio
        cityTv.text = currentUser.city
        countryTv.text = currentUser.country
        genderToMeetTv.text = currentUser.genderToMeet
        minAgeTv.text = currentUser.minAge.toString()
        maxAgeTv.text = currentUser.maxAge.toString()
        student.isChecked = currentUser.student
        if (student.isChecked) {
            switchVisibility(college)
            collegeTv.text = currentUser.college
        }
        zodiacTv.text = currentUser.zodiac
        birthDateTv.text = currentUser.birthDate


        firstName.setOnClickListener {
            switchVisibility(editFirstNameRl)
        }

        firstNameDone.setOnClickListener {
            val fNamVal = editFirstNameEt.text.toString()
            if (fNamVal.compareTo("", false) == 0)
                Toast.makeText(this, "Field can not be empty", Toast.LENGTH_SHORT).show()
            else {
                currentUser.firstName = fNamVal
                firstNameTv.text = fNamVal
                editFirstNameRl.visibility = View.GONE
            }
        }

        lastName.setOnClickListener {
            switchVisibility(editLastNameRl)
        }

        lastNameDone.setOnClickListener {
            val lName = editLastNameEt.text.toString()
            if (lName.compareTo("") == 0)
                Toast.makeText(this, "Field can not be empty", Toast.LENGTH_SHORT).show()
            else {
                currentUser.lastName = lName
                lastNameTv.text = lName
                editLastNameRl.visibility = View.GONE
            }
        }

        gender.setOnClickListener {
            switchVisibility(editGenderRl)
        }

        male.setOnClickListener {
            editGenderRl.visibility = View.GONE
            val startColor = ContextCompat.getColor(this, R.color.black)
            val pink = ContextCompat.getColor(this, R.color.md_pink_600)
            val endColor = ContextCompat.getColor(this, R.color.md_blue_600)
            animateImageTintChange(male, startColor, endColor)
            animateImageTintChange(female, pink, startColor)
            genderTv.text = "Male"
            currentUser.gender = "Male"
        }

        female.setOnClickListener {
            editGenderRl.visibility = View.GONE
            val startColor = ContextCompat.getColor(this, R.color.black)
            val endColor = ContextCompat.getColor(this, R.color.md_pink_600)
            val blue = ContextCompat.getColor(this, R.color.md_blue_600)
            animateImageTintChange(female, startColor, endColor)
            animateImageTintChange(male, blue, startColor)
            genderTv.text = "Female"
            currentUser.gender = "Female"
        }

        bio.setOnClickListener {
            switchVisibility(editBioRl)
        }

        bioDone.setOnClickListener {
            val bioVal = editBioEt.text.toString()
            if (bioVal.compareTo("") == 0)
                Toast.makeText(this, "Field can not be empty", Toast.LENGTH_SHORT).show()
            else {
                currentUser.bio = bioVal
                bioTv.text = bioVal
                switchVisibility(editBioRl)
            }
        }

        city.setOnClickListener {
            switchVisibility(editCityRl)
        }

        cityDone.setOnClickListener {
            val cityVal = editCityEt.text.toString()
            if (cityVal.compareTo("") == 0)
                Toast.makeText(this, "Field can not be empty", Toast.LENGTH_SHORT).show()
            else {
                currentUser.city = cityVal
                cityTv.text = cityVal
                switchVisibility(editCityRl)
            }
        }

        country.setOnClickListener {
            switchVisibility(editCountryRl)
        }

        editCountryEt.setOnClickListener {
            val countryVal = editCountryEt.text.toString()
            if (countryVal.compareTo("") == 0)
                Toast.makeText(this, "Field can not be empty", Toast.LENGTH_SHORT).show()
            else {
                currentUser.country = countryVal
                countryTv.text = countryVal
                switchVisibility(editCountryRl)
            }
        }

        genderToMeet.setOnClickListener {
            switchVisibility(editGenderToMeetRl)
        }

        genderToMeetMale.setOnClickListener {
            editGenderToMeetRl.visibility = View.GONE
            val startColor = ContextCompat.getColor(this, R.color.black)
            val pink = ContextCompat.getColor(this, R.color.md_pink_600)
            val endColor = ContextCompat.getColor(this, R.color.md_blue_600)
            animateImageTintChange(genderToMeetMale, startColor, endColor)
            animateImageTintChange(genderToMeetFemale, pink, startColor)
            genderToMeetTv.text = "Male"
            currentUser.genderToMeet = "Male"
        }

        genderToMeetFemale.setOnClickListener {
            editGenderToMeetRl.visibility = View.GONE
            val startColor = ContextCompat.getColor(this, R.color.black)
            val endColor = ContextCompat.getColor(this, R.color.md_pink_600)
            val blue = ContextCompat.getColor(this, R.color.md_blue_600)
            animateImageTintChange(genderToMeetFemale, startColor, endColor)
            animateImageTintChange(genderToMeetMale, blue, startColor)
            genderToMeetTv.text = "Female"
            currentUser.genderToMeet = "Female"
        }

        minAge.setOnClickListener {
            switchVisibility(editMinAgeRl)
        }

        minAgeDone.setOnClickListener {
            val minAgeVal = editMinAgeEt.text.toString()
            if (minAgeVal.compareTo("") == 0)
                Toast.makeText(this, "Field can not be empty", Toast.LENGTH_SHORT).show()
            else {
                currentUser.minAge = minAgeVal.toInt()
                minAgeTv.text = minAgeVal
                switchVisibility(editMinAgeRl)
            }
        }

        maxAge.setOnClickListener {
            switchVisibility(editMaxAgeRl)
        }

        maxAgeDone.setOnClickListener {
            val maxAgeVal = editMaxAgeEt.text.toString()
            if (maxAgeVal.compareTo("") == 0)
                Toast.makeText(this, "Field can not be empty", Toast.LENGTH_SHORT).show()
            else {
                currentUser.maxAge = maxAgeVal.toInt()
                maxAgeTv.text = maxAgeVal
                switchVisibility(editMaxAgeRl)
            }
        }

        student.setOnCheckedChangeListener { _, b ->
            currentUser.student = b
            if (b) {
                college.visibility = View.VISIBLE
            }
            else {
                if (editCollegeRl.visibility == View.VISIBLE)
                    editCollegeRl.visibility = View.GONE
                college.visibility = View.GONE
            }
        }

        college.setOnClickListener {
            switchVisibility(editCollegeRl)
        }

        collegeDone.setOnClickListener {
            val collegeName = editCollegeEt.text.toString()
            if (collegeName.compareTo("") == 0)
                Toast.makeText(this, "Field can not be empty", Toast.LENGTH_SHORT).show()
            else {
                currentUser.college = collegeName
                collegeTv.text = collegeName
                switchVisibility(editCollegeRl)
            }
        }

        zodiac.setOnClickListener {
            switchVisibility(editZodiacRl)
        }

        zodiacDone.setOnClickListener {
            val zodiacVal = editZodiacEt.text.toString()
            if (zodiacVal.compareTo("") == 0)
                Toast.makeText(this, "Field can not be empty", Toast.LENGTH_SHORT).show()
            else {
                currentUser.zodiac = zodiacVal
                zodiacTv.text = zodiacVal
                switchVisibility(editZodiacRl)
            }
        }

        birthDate.setOnClickListener {
            switchVisibility(editBirthDateRl)
        }

        editBirthDateEt.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(this@EditUserProfileActivity,
                DatePickerDialog.OnDateSetListener { _, y, monthOfYear, dayOfMonth ->
                    val m: String = if (monthOfYear < 10)
                        "0$monthOfYear"
                    else
                        monthOfYear.toString() + ""
                    val d: String = if (dayOfMonth < 10)
                        "0$dayOfMonth"
                    else
                        dayOfMonth.toString() + ""
                    editBirthDateEt.setText("$m/$d/$y")
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.create()
            datePickerDialog.show()
        }

        birthDateDone.setOnClickListener {
            val bDay = editBirthDateEt.text.toString()
            if (bDay.compareTo("") == 0)
                Toast.makeText(this, "Field can not be empty", Toast.LENGTH_SHORT).show()
            else {
                currentUser.birthDate = bDay
                birthDateTv.text = bDay
                switchVisibility(editBirthDateRl)
            }
        }

        back.setOnClickListener {
            finish()
        }

        done.setOnClickListener {
            updateUserData()
        }
    }

    private fun updateUserData() {
        val client = AsyncHttpClient()
        val params = RequestParams()

        params.put("_id", currentUser._id)
        params.put("email", currentUser.email)
        params.put("password", currentUser.password)
        params.put("firstName", currentUser.firstName)
        params.put("lastName", currentUser.lastName)
        params.put("bio", currentUser.bio)
        params.put("gender", currentUser.gender)
        params.put("age", currentUser.age)
        params.put("city", currentUser.city)
        params.put("country", currentUser.country)
        params.put("eatTimePeriods", currentUser.eatTimePeriods)
        params.put("eatTimes", currentUser.eatTimes)
        params.put("birthDate", currentUser.birthDate)
        params.put("genderToMeet", currentUser.genderToMeet)
        params.put("maxAge", currentUser.maxAge)
        params.put("minAge", currentUser.minAge)
        params.put("profileSetupComplete", currentUser.profileSetupComplete)
        params.put("student", currentUser.student)
        params.put("zodiac", currentUser.zodiac)
        params.put("college", currentUser.college)
        params.put("deviceToken", currentUser.deviceToken)

        client.post(dbLinks.updateUserData, params, object: JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val status = response!!.getInt("status")

                if (status == HttpStatus.SC_OK) {
                    runOnUiThread {
                        Toast.makeText(this@EditUserProfileActivity, "Data updated", Toast.LENGTH_SHORT).show()
                        intent.putExtra("currentUser", currentUser)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }
            }
        })
    }

    private fun reqProfilePhoto(userId: String) {
        val client = AsyncHttpClient()
        val params = RequestParams()

        params.put("ids", userId)

        client.get(dbLinks.smallProfileImagesById, params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                super.onSuccess(statusCode, headers, response)

                val status = response!!.getInt("status")

                if (status == HttpStatus.SC_OK) {
                    val userImageArray = response.getJSONArray("userImages")
                    if (userImageArray.length() > 0) {
                        val imageId = userImageArray.getJSONObject(0).getString("_id")
                        currentUser.profileImageId = imageId
                        runOnUiThread {
                            Glide.with(this@EditUserProfileActivity).load(dbLinks
                                .getImageNormal(currentUser._id, currentUser.profileImageId)).into(profileImage)
                        }
                    }
                }
            }
        })
    }

    private fun bindViews() {
        changeProfilePicture = findViewById(R.id.rl_change_profile_pic)
        back = findViewById(R.id.iv_back)
        done = findViewById(R.id.btn_done)
        profileImage = findViewById(R.id.iv_profile_image)
        firstName = findViewById(R.id.rl_first_name)
        firstNameTv = findViewById(R.id.tv_first_name)
        editFirstNameRl = findViewById(R.id.rl_edit_first_name)
        editFirstNameEt = findViewById(R.id.et_first_name)
        firstNameDone = findViewById(R.id.fab_first_name_done)
        lastName = findViewById(R.id.rl_last_name)
        lastNameTv = findViewById(R.id.tv_last_name)
        editLastNameRl = findViewById(R.id.rl_edit_last_name)
        editLastNameEt = findViewById(R.id.et_last_name)
        lastNameDone = findViewById(R.id.fab_last_name_done)
        gender = findViewById(R.id.rl_gender)
        genderTv = findViewById(R.id.tv_gender)
        editGenderRl = findViewById(R.id.rl_edit_gender)
        male = findViewById(R.id.iv_gender_male)
        female = findViewById(R.id.iv_gender_female)
        bio = findViewById(R.id.rl_bio)
        bioTv = findViewById(R.id.tv_bio)
        editBioRl = findViewById(R.id.rl_edit_bio)
        editBioEt = findViewById(R.id.et_bio)
        bioDone = findViewById(R.id.fab_bio_done)
        city = findViewById(R.id.rl_city)
        cityTv = findViewById(R.id.tv_city)
        editCityRl = findViewById(R.id.rl_edit_city)
        editCityEt = findViewById(R.id.et_city)
        cityDone = findViewById(R.id.fab_city_done)
        country = findViewById(R.id.rl_country)
        countryTv = findViewById(R.id.tv_country)
        editCountryRl = findViewById(R.id.rl_edit_country)
        editCountryEt = findViewById(R.id.et_country)
        countryDone = findViewById(R.id.fab_country_done)
        genderToMeet = findViewById(R.id.rl_gender_to_meet)
        genderToMeetTv = findViewById(R.id.tv_gender_to_meet)
        editGenderToMeetRl = findViewById(R.id.rl_edit_gender_to_meet)
        genderToMeetMale = findViewById(R.id.iv_gender_to_meet_male)
        genderToMeetFemale = findViewById(R.id.iv_gender_to_meet_female)
        minAge = findViewById(R.id.rl_min_age)
        minAgeTv = findViewById(R.id.tv_min_age)
        editMinAgeRl = findViewById(R.id.rl_edit_min_age)
        editMinAgeEt = findViewById(R.id.et_min_age)
        minAgeDone = findViewById(R.id.fab_min_age_done)
        maxAge = findViewById(R.id.rl_max_age)
        maxAgeTv = findViewById(R.id.tv_max_age)
        editMaxAgeRl = findViewById(R.id.rl_edit_max_age)
        editMaxAgeEt = findViewById(R.id.et_max_age)
        maxAgeDone = findViewById(R.id.fab_max_age_done)
        student = findViewById(R.id.cb_student)
        college = findViewById(R.id.rl_college)
        collegeTv = findViewById(R.id.tv_college)
        editCollegeRl = findViewById(R.id.rl_edit_college)
        editCollegeEt = findViewById(R.id.et_college)
        collegeDone = findViewById(R.id.fab_college_done)
        zodiac = findViewById(R.id.rl_zodiac)
        zodiacTv = findViewById(R.id.tv_zodiac)
        editZodiacEt = findViewById(R.id.et_zodiac)
        editZodiacRl = findViewById(R.id.rl_edit_zodiac)
        zodiacDone = findViewById(R.id.fab_zodiac_done)
        birthDate = findViewById(R.id.rl_birth_date)
        birthDateTv = findViewById(R.id.tv_birth_date)
        editBirthDateEt = findViewById(R.id.et_birth_date)
        editBirthDateRl = findViewById(R.id.rl_edit_birth_date)
        birthDateDone = findViewById(R.id.fab_birth_date_done)
    }

    private fun switchVisibility(view: View) {
        if (view.visibility == View.VISIBLE)
            view.visibility = View.GONE
        else
            view.visibility = View.VISIBLE
    }

    private fun animateImageTintChange(view: ImageView, startColor: Int, endColor: Int) {
        val animator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
        animator.duration = 200
        animator.addUpdateListener {
            view.setColorFilter(it.animatedValue as Int)
        }
        animator.start()
    }
}
