package com.example.foodbuddy

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.widget.*

class EditUserProfileActivity : AppCompatActivity() {
    companion object {
        const val TAG = "EditUserProfileActivity"
    }

    private lateinit var currentUser: User

    private lateinit var changeProfilePicture: RelativeLayout
    private lateinit var back: ImageView
    private lateinit var done: Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user_profile)

        currentUser = intent.getSerializableExtra("currentUser") as User

        bindViews()

        firstNameTv.text = currentUser.firstName

        firstName.setOnClickListener {
            if (editFirstNameRl.visibility == View.VISIBLE)
                editFirstNameRl.visibility = View.GONE
            else
                editFirstNameRl.visibility = View.VISIBLE
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
    }

    private fun bindViews() {
        changeProfilePicture = findViewById(R.id.rl_change_profile_pic)
        back = findViewById(R.id.iv_back)
        done = findViewById(R.id.btn_done)
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
        editGenderRl = findViewById(R.id.rl_gender)
        male = findViewById(R.id.iv_gender_male)
        female = findViewById(R.id.iv_gender_female)
        bio = findViewById(R.id.rl_bio)
        bioTv = findViewById(R.id.tv_bio)
        editBioRl = findViewById(R.id.rl_edit_bio)
        editBioEt = findViewById(R.id.et_bio)
        bioDone = findViewById(R.id.fab_bio_done)
    }
}
