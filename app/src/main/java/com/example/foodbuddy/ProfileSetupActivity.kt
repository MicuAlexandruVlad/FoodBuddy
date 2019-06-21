package com.example.foodbuddy

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.nineoldandroids.animation.Animator
import com.rengwuxian.materialedittext.MaterialEditText
import java.util.*
import android.R.attr.start
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Base64
import com.bumptech.glide.Glide
import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.loopj.android.http.SyncHttpClient
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.HttpStatus
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList


class ProfileSetupActivity : AppCompatActivity() {

    private var currentPhotoPath = ""
    private val TAG = "ProfileSetupActivity"
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_GALLERY_IMAGE = 2

    private lateinit var parentIntent: Intent
    private lateinit var currentUser: User
    private lateinit var eatTimesList: ArrayList<Int>
    private lateinit var eatTimesAdapter: EatTimesAdapter
    private lateinit var userImage: UserImage

    private lateinit var stepProgress: ProgressBar

    // step 1 ui
    private lateinit var firstStep: RelativeLayout
    private lateinit var q1: TextView
    private lateinit var firstName: MaterialEditText
    private lateinit var lastName: MaterialEditText
    private lateinit var toStep2: Button
    private lateinit var animationUtils: AnimationUtils

    // step 2 ui
    private lateinit var secondStep: RelativeLayout
    private lateinit var q2: TextView
    private lateinit var bio: MaterialEditText
    private lateinit var gender: MaterialEditText
    private lateinit var toStep3: Button

    // step 3 ui
    private lateinit var thirdStep: RelativeLayout
    private lateinit var q3: TextView
    private lateinit var birthDate: MaterialEditText
    private lateinit var age: MaterialEditText
    private lateinit var toStep4: Button
    private lateinit var zodiac: MaterialEditText

    // step 4 ui
    private lateinit var fourthStep: RelativeLayout
    private lateinit var q4: TextView
    private lateinit var country: MaterialEditText
    private lateinit var city: MaterialEditText
    private lateinit var toStep5: Button

    // step 5 ui
    private lateinit var fifthStep: RelativeLayout
    private lateinit var q5: TextView
    private lateinit var student: LinearLayout
    private lateinit var yes: TextView
    private lateinit var no: TextView
    private lateinit var yesRL: RelativeLayout
    private lateinit var noRL: RelativeLayout
    private lateinit var college: MaterialEditText
    private lateinit var toStep6: Button

    // step 6 ui
    private lateinit var sixthStep: RelativeLayout
    private lateinit var q6: TextView
    private lateinit var eatTimesRL: RelativeLayout
    private lateinit var eatTimesSP: Spinner
    private lateinit var eatTimesTV: TextView
    private lateinit var eatTimesEHL: ExpandableHeightListView
    private lateinit var toStep7: Button

    // step 7 ui
    private lateinit var seventhStep: RelativeLayout
    private lateinit var q7: TextView
    private lateinit var genderToMeetLL: LinearLayout
    private lateinit var female: RelativeLayout
    private lateinit var male: RelativeLayout
    private lateinit var minAge: MaterialEditText
    private lateinit var maxAge: MaterialEditText
    private lateinit var toStep8: Button

    // step 8 ui
    private lateinit var eighthStep: RelativeLayout
    private lateinit var q8: TextView
    private lateinit var profileCV: CardView
    private lateinit var profileImage: ImageView
    private lateinit var openCamera: RelativeLayout
    private lateinit var openGallery: RelativeLayout
    private lateinit var finishSetup: Button

    private var isStudent: Boolean = false
    private var isMaleSelected = false
    private var isFemaleSelected = false
    private var isStudentSelected: Boolean = false
    private var genderToMeetSelected = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        supportActionBar?.hide()

        parentIntent = intent
        currentUser = parentIntent.getSerializableExtra("currentUser") as User
        animationUtils = AnimationUtils()
        eatTimesList = ArrayList()
        eatTimesList.add(0)
        eatTimesAdapter = EatTimesAdapter(this, eatTimesList)

        showDialog("Profile Setup", "Time to set up your profile. You will be asked a few questions and after that" +
                " you can start using FoodBuddy")
        bindViews()

        eatTimesEHL.adapter = eatTimesAdapter
        eatTimesEHL.isExpanded = true
        eatTimesEHL.dividerHeight = 0
        eatTimesEHL.divider = null

        stepProgress.max = 8
        animationUtils.generatePbAnim(stepProgress, 0, 1)
        stepProgress.animate()
        stepProgress.interpolator = AccelerateDecelerateInterpolator()
        firstStep.visibility = View.GONE
        secondStep.visibility = View.GONE
        thirdStep.visibility = View.GONE
        fourthStep.visibility = View.GONE
        fifthStep.visibility = View.GONE
        sixthStep.visibility = View.GONE
        seventhStep.visibility = View.GONE
        eighthStep.visibility = View.VISIBLE

        val spinnerArray = ArrayList<Int>()
        spinnerArray.add(1)
        spinnerArray.add(2)
        spinnerArray.add(3)
        spinnerArray.add(4)
        spinnerArray.add(5)

        val spinnerAdapter = ArrayAdapter<Int>(this, R.layout.spinner_item, spinnerArray)
        eatTimesSP.adapter = spinnerAdapter

        eatTimesSP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                insertItemsInEatTimes(p2 + 1)
                if (p2 == 0) {
                    eatTimesTV.text = "time a day"
                }
                else
                    eatTimesTV.text = "times a day"
            }

        }

        firstStepAnimateViewsIn()

        toStep2.setOnClickListener {

            val fNameVal = firstName.text.toString()
            val lNameVal = lastName.text.toString()
            if (fNameVal.compareTo("", false) == 0 || lNameVal.compareTo("", false) == 0)
                Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show()
            else {
                currentUser.firstName = fNameVal
                currentUser.lastName = lNameVal
                animationUtils.generatePbAnim(stepProgress, 1, 2)
                firstStepAnimateViewsOut()
            }
        }

        toStep3.setOnClickListener {
            val bioVal = bio.text.toString()
            val genderVal = gender.text.toString()
            if (bioVal.compareTo("", false) == 0 || genderVal.compareTo("") == 0){
                Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show()
            }
            else {
                animationUtils.generatePbAnim(stepProgress, 2, 3)
                currentUser.bio = bioVal
                currentUser.gender = genderVal
                secondStepAnimateViewsOut()
            }
        }

        birthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(this@ProfileSetupActivity,
                DatePickerDialog.OnDateSetListener { _, y, monthOfYear, dayOfMonth ->
                    val m: String = if (monthOfYear < 10)
                        "0$monthOfYear"
                    else
                        monthOfYear.toString() + ""
                    val d: String = if (dayOfMonth < 10)
                        "0$dayOfMonth"
                    else
                        dayOfMonth.toString() + ""
                    birthDate.setText("$m/$d/$y")
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.create()
            datePickerDialog.show()
        }

        toStep4.setOnClickListener {
            val bDayVal = birthDate.text.toString()
            val ageVal = age.text.toString()
            val zVal = zodiac.text.toString()
            if (bDayVal.compareTo("", false) == 0 ||
                ageVal.compareTo("", false) == 0 || zVal.compareTo("", false) == 0) {
                Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show()
            }
            else {
                animationUtils.generatePbAnim(stepProgress, 3, 4)
                currentUser.birthDate = bDayVal
                currentUser.age = ageVal.toInt()
                currentUser.zodiac = zVal
                thirdStepAnimateViewsOut()
            }
        }

        toStep5.setOnClickListener {
            val countryVal = country.text.toString()
            val cityVal = city.text.toString()
            if (countryVal.compareTo("", false) == 0 || cityVal.compareTo("", false) == 0) {
                Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show()
            }
            else {
                animationUtils.generatePbAnim(stepProgress, 4, 5)
                currentUser.city = cityVal
                currentUser.country = countryVal
                fourthStepAnimateViewsOut()
            }
        }

        yes.setOnClickListener {
            val white = ContextCompat.getColor(this@ProfileSetupActivity, R.color.white)
            val sunsetOrange = ContextCompat.getColor(this@ProfileSetupActivity, R.color.sunsetOrange)
            if (isStudentSelected) {
                animateElevation(yesRL, 0f, 12f)
                animateElevation(noRL, 12f, 0f)
                animateBackgroundColor(yesRL, white, sunsetOrange)
                animateBackgroundColor(noRL, sunsetOrange, white)
            }
            else {
                animateElevation(yesRL, 0f, 12f)
                animateBackgroundColor(yesRL, white, sunsetOrange)
            }
            isStudentSelected = true
            isStudent = true
            college.visibility = View.VISIBLE
        }

        no.setOnClickListener {
            val white = ContextCompat.getColor(this@ProfileSetupActivity, R.color.white)
            val sunsetOrange = ContextCompat.getColor(this@ProfileSetupActivity, R.color.sunsetOrange)
            if (isStudentSelected) {
                animateElevation(yesRL, 12f, 0f)
                animateElevation(noRL, 0f, 12f)
                animateBackgroundColor(noRL, white, sunsetOrange)
                animateBackgroundColor(yesRL, sunsetOrange, white)
            }
            else {
                animateElevation(noRL, 0f, 12f)
                animateBackgroundColor(noRL, white, sunsetOrange)
            }
            isStudentSelected = true
            isStudent = false
            college.visibility = View.GONE
            Log.d(TAG, "no clicked")

        }

        toStep6.setOnClickListener {
            if (isStudentSelected) {
                if (isStudent) {
                    val collegeVal = college.text.toString()
                    if (collegeVal.compareTo("", false) == 0 )
                        Toast.makeText(this, "College fields is empty", Toast.LENGTH_SHORT).show()
                    else {
                        animationUtils.generatePbAnim(stepProgress, 5, 6)
                        currentUser.student = true
                        currentUser.college = collegeVal

                        fifthStepAnimateViewsOut()
                    }
                }
                else {
                    animationUtils.generatePbAnim(stepProgress, 5, 6)
                    currentUser.student = false

                    fifthStepAnimateViewsOut()
                }
            }
            else {
                Toast.makeText(this, "Student option not selected", Toast.LENGTH_SHORT).show()
            }
        }

        toStep7.setOnClickListener {
            val sb = StringBuilder()
            var p = 1
            val eatTimes = eatTimesList[eatTimesSP.selectedItemPosition]
            for (index in 0 until eatTimesList.size) {
                val itemView = eatTimesEHL.getChildAt(index)
                val startTime = itemView.findViewById<MaterialEditText>(R.id.met_start_time)
                val endTime = itemView.findViewById<MaterialEditText>(R.id.met_end_time)
                val sVal = startTime.text.toString()
                val eVal = endTime.text.toString()

                if (sVal.compareTo("", false) == 0 || eVal.compareTo("", false) == 0) {
                    Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show()
                    p = 0
                    break
                }
                else {
                    val eatTime = "$sVal - $eVal"
                    sb.append(eatTime)
                    if (index < eatTimesList.size - 1)
                        sb.append("/")
                }
            }
            if (p == 1) {
                currentUser.eatTimes = eatTimes
                currentUser.eatTimePeriods = sb.toString()
                sixthStepAnimateViewsOut()
                animationUtils.generatePbAnim(stepProgress, 6, 7)
            }

            Log.d(TAG, "eat times -> " + eatTimesList.size)
            Log.d(TAG, "eat time periods -> $sb")
        }

        male.setOnClickListener {
            if (male.elevation.toInt() == 0) {
                if (genderToMeetSelected) {
                    animateElevation(female, 16f, 0f)
                    animateElevation(male, 0f, 16f)
                }
                else
                    animateElevation(male, 0f, 16f)
            }
            genderToMeetSelected = true
            isMaleSelected = true
            isFemaleSelected = false
        }

        female.setOnClickListener {
            if (female.elevation.toInt() == 0) {
                if (genderToMeetSelected) {
                    animateElevation(male, 16f, 0f)
                    animateElevation(female, 0f, 16f)
                }
                else
                    animateElevation(female, 0f, 16f)
            }
            genderToMeetSelected = true
            isFemaleSelected = true
            isMaleSelected = false
        }

        toStep8.setOnClickListener {
            if (genderToMeetSelected) {
                val min = minAge.text.toString()
                val max = maxAge.text.toString()

                if (isMaleSelected)
                    currentUser.genderToMeet = "Male"
                else
                    currentUser.genderToMeet = "Female"

                if (min.compareTo("") == 0 || max.compareTo("") == 0)
                    Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show()
                else {
                    currentUser.minAge = min.toInt()
                    currentUser.maxAge = max.toInt()
                    seventhStepAnimateViewsOut()
                    animationUtils.generatePbAnim(stepProgress, 7, 8)
                }
            }
            else
                Toast.makeText(this, "Gender is not selected", Toast.LENGTH_SHORT).show()
        }

        openCamera.setOnClickListener {
            if (checkCameraHardware(this)) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), 21)
                }
                else {
                    dispatchTakePictureIntent()
                }
            }
            else {
                Toast.makeText(this, "This device does not have a camera", Toast.LENGTH_SHORT).show()
            }
        }

        openGallery.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, REQUEST_GALLERY_IMAGE)
        }

        finishSetup.setOnClickListener {
            if (profileImage.drawable != null) {
                currentUser.profileSetupComplete = true

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

                val dbLinks = DBLinks()

                client.post(dbLinks.updateUserData, params, object: JsonHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                        super.onSuccess(statusCode, headers, response)

                        val status = response!!.getInt("status")

                        if (status == HttpStatus.SC_OK) {
                            // display success dialog
                            val builder = AlertDialog.Builder(this@ProfileSetupActivity)
                                .setTitle("Success")
                                .setMessage("You have finished setting up your profile. Now you can fully experience FoodBuddy")
                                .setNeutralButton("OK") {dialogInterface, _ ->
                                    dialogInterface.dismiss()
                                    val intent = Intent(applicationContext, MainActivity::class.java)
                                    intent.putExtra("currentUser", currentUser)
                                    intent.putExtra("fromProfileSetup", true)
                                    startActivity(intent)
                                    finish()
                                }
                            builder.create()
                            builder.show()

                        }
                        else {
                            Toast.makeText(applicationContext, "There was a problem", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?,
                                           errorResponse: JSONObject?
                    ) {
                        super.onFailure(statusCode, headers, throwable, errorResponse)
                        Log.d(TAG, "Error retrieving data from server: -> " + errorResponse.toString())
                    }
                })
            }
            else
                Toast.makeText(this, "Profile picture not added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileproviderfoodbuddy",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) as File
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
            Log.d(TAG, "photo path -> $currentPhotoPath")
        }
    }

    fun getPathFromURI(contentUri: Uri): String? {
        var res: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(contentUri, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            res = cursor.getString(column_index)
        }
        cursor.close()
        return res
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // TODO: if screen is on landscape and you take a picture and come back to this activity says originalBmp is null
            // (maybe from recreating activity)
            var originalBmp = BitmapFactory.decodeFile(currentPhotoPath)
            Glide.with(this).load(originalBmp).centerCrop().into(profileImage)
            // saveImageLocally(originalBmp)


            Thread(Runnable {
                val bao = ByteArrayOutputStream()
                val smallBmp = Bitmap.createScaledBitmap(originalBmp, originalBmp.width / 5,
                    originalBmp.height / 5, false)

                smallBmp.compress(Bitmap.CompressFormat.JPEG, 80, bao)
                val smallBmpBytes = bao.toByteArray()

                Log.d(TAG, "camera image byte count -> " + originalBmp.byteCount)
                Log.d(TAG, "camera image height before loop -> " + originalBmp.height)
                while (originalBmp.width * originalBmp.height > 3686400) {
                    originalBmp = Bitmap.createScaledBitmap(originalBmp, (originalBmp.width / 1.2).toInt(),
                        (originalBmp.height / 1.2).toInt(), false)
                }
                Log.d(TAG, "camera image byte count after loop -> " + originalBmp.byteCount)
                Log.d(TAG, "camera image height after loop -> " + originalBmp.height)
                val bao1 = ByteArrayOutputStream()
                originalBmp.compress(Bitmap.CompressFormat.JPEG, 50, bao1)
                Log.d(TAG, "camera image byte count after compression -> " + originalBmp.byteCount)
                val normalResBmpBytes = bao1.toByteArray()

                val smallEncodedImage = Base64.encodeToString(smallBmpBytes, Base64.DEFAULT)
                val normalEncodedImage = Base64.encodeToString(normalResBmpBytes, Base64.DEFAULT)

                val file = File(currentPhotoPath)
                userImage = UserImage()
                userImage.imageName = file.nameWithoutExtension
                userImage.isProfileImage = true
                userImage.smallProfileImageData = smallEncodedImage
                userImage.normalProfileImageData = normalEncodedImage
                userImage.userId = currentUser._id

                val params = RequestParams()
                params.put("isProfileImage", userImage.isProfileImage)
                params.put("smallProfileImageData", userImage.smallProfileImageData)
                params.put("normalProfileImageData", userImage.normalProfileImageData)
                params.put("userId", userImage.userId)
                params.put("imageName", userImage.imageName)
                postImage(params)

            }).start()
        }

        if (requestCode == REQUEST_GALLERY_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null ) {
                val uri = data.data
                var galleryBitmap = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
                Glide.with(this).load(galleryBitmap).centerCrop().into(profileImage)

                Thread(Runnable {

                    val smallBao = ByteArrayOutputStream()
                    val normalBao = ByteArrayOutputStream()

                    val smallBmp = Bitmap.createScaledBitmap(galleryBitmap, galleryBitmap.width / 4,
                        galleryBitmap.height / 4, false)

                    smallBmp.compress(Bitmap.CompressFormat.JPEG, 80, smallBao)
                    val smallBmpBytes = smallBao.toByteArray()

                    Log.d(TAG, "gallery image height -> " + galleryBitmap.height)
                    while (galleryBitmap.width * galleryBitmap.height > 3686400) {
                        galleryBitmap = Bitmap.createScaledBitmap(galleryBitmap, (galleryBitmap.width / 1.2).toInt(),
                            (galleryBitmap.height / 1.2).toInt(), false)
                    }
                    Log.d(TAG, "gallery image height after loop -> " + galleryBitmap.height)
                    Log.d(TAG, "gallery image byte count before compression -> " + galleryBitmap.byteCount)
                    galleryBitmap.compress(Bitmap.CompressFormat.JPEG, 50, normalBao)
                    Log.d(TAG, "gallery image byte count after compression -> " + galleryBitmap.byteCount)
                    val normalResBmpBytes = normalBao.toByteArray()

                    val smallEncodedImage = Base64.encodeToString(smallBmpBytes, Base64.DEFAULT)
                    val normalEncodedImage = Base64.encodeToString(normalResBmpBytes, Base64.DEFAULT)


                    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

                    userImage = UserImage()
                    userImage.imageName = "JPEG_$timeStamp"
                    userImage.isProfileImage = true
                    userImage.smallProfileImageData = smallEncodedImage
                    userImage.normalProfileImageData = normalEncodedImage
                    userImage.userId = currentUser._id

                    Log.d(TAG, "image name -> " + userImage.imageName)

                    val params = RequestParams()
                    params.put("isProfileImage", userImage.isProfileImage)
                    params.put("smallProfileImageData", userImage.smallProfileImageData)
                    params.put("normalProfileImageData", userImage.normalProfileImageData)
                    params.put("userId", userImage.userId)
                    params.put("imageName", userImage.imageName)
                    postImage(params)

                }).start()
            }
        }
    }

    private fun postImage(params: RequestParams) {
        val dbLinks = DBLinks()
        val client = SyncHttpClient()


        client.post(dbLinks.uploadUserImage, params, object : JsonHttpResponseHandler() {
            override fun onStart() {
                super.onStart()
            }

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                super.onSuccess(statusCode, headers, response)
                Log.d(TAG, "Image uploaded")
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                throwable: Throwable?,
                errorResponse: JSONObject?
            ) {
                super.onFailure(statusCode, headers, throwable, errorResponse)
            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveImageLocally(bmp: Bitmap) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        try {
            val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
            )
            val out = FileOutputStream(image)
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out)
            currentPhotoPath = image.absolutePath
            writeToSharedPreferences(currentPhotoPath)
            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun writeToSharedPreferences(value: String) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(getString(R.string.profilePicturePath), value)
            apply()
        }
    }

    private fun firstStepAnimateViewsIn() {
        val fadeInQ1 = animationUtils.generateFadeInAnimation(500, 0)
        val fadeInFirstName = animationUtils.generateFadeInAnimation(500, 150)
        val fadeInLastName = animationUtils.generateFadeInAnimation(500, 300)
        q1.startAnimation(fadeInQ1)
        firstName.startAnimation(fadeInFirstName)
        lastName.startAnimation(fadeInLastName)
    }

    private fun firstStepAnimateViewsOut() {
        val fadeOutQ1 = animationUtils.generateFadeOutAnimation(500, 0)
        val fadeOutFirstName = animationUtils.generateFadeOutAnimation(500, 150)
        val fadeOutLastName = animationUtils.generateFadeOutAnimation(500, 300)
        val fadeOutBtn = animationUtils.generateFadeOutAnimation(300, 400)
        val fadeOutParent = animationUtils.generateFadeOutAnimation(1, 800)
        q1.startAnimation(fadeOutQ1)
        firstName.startAnimation(fadeOutFirstName)
        lastName.startAnimation(fadeOutLastName)
        toStep2.startAnimation(fadeOutBtn)
        firstStep.startAnimation(fadeOutParent)

        fadeOutParent.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                firstName.visibility = View.GONE
                lastName.visibility = View.GONE
                firstStep.visibility = View.GONE
                secondStepAnimateViewsIn()
            }

            override fun onAnimationStart(p0: Animation?) {

            }

        })
    }

    private fun secondStepAnimateViewsIn() {
        secondStep.visibility = View.VISIBLE
        val fadeInQ = animationUtils.generateFadeInAnimation(500, 0)
        val fadeInBio = animationUtils.generateFadeInAnimation(500, 150)
        val fadeInGender = animationUtils.generateFadeInAnimation(500, 300)
        val fadeInBtn = animationUtils.generateFadeInAnimation(500, 450)
        q2.startAnimation(fadeInQ)
        bio.startAnimation(fadeInBio)
        gender.startAnimation(fadeInGender)
        toStep3.startAnimation(fadeInBtn)
    }

    private fun secondStepAnimateViewsOut() {
        val fadeOutQ2 = animationUtils.generateFadeOutAnimation(500, 0)
        val fadeOutBio = animationUtils.generateFadeOutAnimation(500, 150)
        val fadeOutGender = animationUtils.generateFadeOutAnimation(500, 300)
        val fadeOutBtn = animationUtils.generateFadeOutAnimation(300, 450)
        val fadeOutParent = animationUtils.generateFadeOutAnimation(1, 750)
        q2.startAnimation(fadeOutQ2)
        bio.startAnimation(fadeOutBio)
        gender.startAnimation(fadeOutGender)
        toStep3.startAnimation(fadeOutBtn)
        secondStep.startAnimation(fadeOutParent)

        fadeOutParent.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                bio.visibility = View.GONE
                gender.visibility = View.GONE
                secondStep.visibility = View.GONE
                thirdStepAnimateViewsIn()
            }

            override fun onAnimationStart(p0: Animation?) {

            }

        })
    }

    private fun thirdStepAnimateViewsIn() {
        thirdStep.visibility = View.VISIBLE
        val fadeInQ = animationUtils.generateFadeInAnimation(500, 0)
        val fadeInBirthDate = animationUtils.generateFadeInAnimation(500, 150)
        val fadeInAge = animationUtils.generateFadeInAnimation(500, 300)
        val fadeInZodiac = animationUtils.generateFadeInAnimation(500, 450)
        val fadeInBtn = animationUtils.generateFadeInAnimation(500, 600)
        q3.startAnimation(fadeInQ)
        birthDate.startAnimation(fadeInBirthDate)
        age.startAnimation(fadeInAge)
        zodiac.startAnimation(fadeInZodiac)
        toStep4.startAnimation(fadeInBtn)
    }

    private fun thirdStepAnimateViewsOut() {
        val fadeOutQ3 = animationUtils.generateFadeOutAnimation(500, 0)
        val fadeOutBirthDate = animationUtils.generateFadeOutAnimation(500, 150)
        val fadeOutBirthAge = animationUtils.generateFadeOutAnimation(500, 300)
        val fadeOutZodiac = animationUtils.generateFadeOutAnimation(500, 450)
        val fadeOutBtn = animationUtils.generateFadeOutAnimation(300, 600)
        val fadeOutParent = animationUtils.generateFadeOutAnimation(1, 950)
        q3.startAnimation(fadeOutQ3)
        birthDate.startAnimation(fadeOutBirthDate)
        age.startAnimation(fadeOutBirthAge)
        zodiac.startAnimation(fadeOutZodiac)
        toStep4.startAnimation(fadeOutBtn)
        thirdStep.startAnimation(fadeOutParent)

        fadeOutParent.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                birthDate.visibility = View.GONE
                age.visibility = View.GONE
                zodiac.visibility = View.GONE
                thirdStep.visibility = View.GONE
                fourthStepAnimateViewsIn()
            }

            override fun onAnimationStart(p0: Animation?) {

            }

        })
    }

    private fun fourthStepAnimateViewsIn() {
        fourthStep.visibility = View.VISIBLE
        val fadeInQ = animationUtils.generateFadeInAnimation(500, 0)
        val fadeInCountry = animationUtils.generateFadeInAnimation(500, 150)
        val fadeInCity = animationUtils.generateFadeInAnimation(500, 300)
        val fadeInBtn = animationUtils.generateFadeInAnimation(500, 450)
        q4.startAnimation(fadeInQ)
        country.startAnimation(fadeInCountry)
        city.startAnimation(fadeInCity)
        toStep4.startAnimation(fadeInBtn)
    }

    private fun fourthStepAnimateViewsOut() {
        val fadeOutQ4 = animationUtils.generateFadeOutAnimation(500, 0)
        val fadeOutCountry = animationUtils.generateFadeOutAnimation(500, 150)
        val fadeOutCity = animationUtils.generateFadeOutAnimation(500, 300)
        val fadeOutBtn = animationUtils.generateFadeOutAnimation(300, 450)
        val fadeOutParent = animationUtils.generateFadeOutAnimation(1, 750)
        q4.startAnimation(fadeOutQ4)
        country.startAnimation(fadeOutCountry)
        city.startAnimation(fadeOutCity)
        toStep5.startAnimation(fadeOutBtn)
        fourthStep.startAnimation(fadeOutParent)

        fadeOutParent.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                country.visibility = View.GONE
                city.visibility = View.GONE
                fourthStep.visibility = View.GONE
                fifthStepAnimateViewsIn()
            }

            override fun onAnimationStart(p0: Animation?) {

            }

        })
    }

    private fun fifthStepAnimateViewsIn() {
        fifthStep.visibility = View.VISIBLE
        val fadeInQ = animationUtils.generateFadeInAnimation(500, 0)
        val fadeInStudent = animationUtils.generateFadeInAnimation(500, 150)
        val fadeInBtn = animationUtils.generateFadeInAnimation(500, 300)
        q5.startAnimation(fadeInQ)
        student.startAnimation(fadeInStudent)
        toStep6.startAnimation(fadeInBtn)
    }

    private fun fifthStepAnimateViewsOut() {
        val fadeOutQ = animationUtils.generateFadeOutAnimation(500, 0)
        val fadeOutStudent = animationUtils.generateFadeOutAnimation(500, 150)
        val fadeOutCollege = animationUtils.generateFadeOutAnimation(500, 300)
        val fadeOutBtn = animationUtils.generateFadeOutAnimation(300, 400)
        val fadeOutParent = animationUtils.generateFadeOutAnimation(1, 800)
        q5.startAnimation(fadeOutQ)
        student.startAnimation(fadeOutStudent)
        if (college.visibility == View.VISIBLE) {
            college.startAnimation(fadeOutCollege)
        }
        toStep6.startAnimation(fadeOutBtn)
        fifthStep.startAnimation(fadeOutParent)

        fadeOutParent.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                student.visibility = View.GONE
                college.visibility = View.GONE
                fifthStep.visibility = View.GONE
                sixthStepAnimateViewsIn()
            }

            override fun onAnimationStart(p0: Animation?) {

            }

        })
    }

    private fun sixthStepAnimateViewsIn() {
        sixthStep.visibility = View.VISIBLE
        val fadeInQ = animationUtils.generateFadeInAnimation(500, 0)
        val fadeInEatTimes = animationUtils.generateFadeInAnimation(500, 150)
        val fadeInBtn = animationUtils.generateFadeInAnimation(500, 300)
        q6.startAnimation(fadeInQ)
        eatTimesRL.startAnimation(fadeInEatTimes)
        toStep7.startAnimation(fadeInBtn)
    }

    private fun sixthStepAnimateViewsOut() {
        val fadeOutQ = animationUtils.generateFadeOutAnimation(500, 0)
        val fadeOutEatTimes = animationUtils.generateFadeOutAnimation(500, 150)
        val fadeOutEatTimesList = animationUtils.generateFadeOutAnimation(500, 300)
        val fadeOutBtn = animationUtils.generateFadeOutAnimation(300, 400)
        val fadeOutParent = animationUtils.generateFadeOutAnimation(1, 800)
        q6.startAnimation(fadeOutQ)
        eatTimesRL.startAnimation(fadeOutEatTimes)
        eatTimesEHL.startAnimation(fadeOutEatTimesList)
        toStep7.startAnimation(fadeOutBtn)
        sixthStep.startAnimation(fadeOutParent)

        fadeOutParent.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                eatTimesRL.visibility = View.GONE
                eatTimesEHL.visibility = View.GONE
                sixthStep.visibility = View.GONE
                seventhStepAnimateViewsIn()
            }

            override fun onAnimationStart(p0: Animation?) {

            }

        })
    }

    private fun seventhStepAnimateViewsIn() {
        seventhStep.visibility = View.VISIBLE
        val fadeInQ = animationUtils.generateFadeInAnimation(500, 0)
        val fadeInGender = animationUtils.generateFadeInAnimation(500, 150)
        val fadeInMinAge = animationUtils.generateFadeInAnimation(500, 300)
        val fadeInMaxAge = animationUtils.generateFadeInAnimation(500, 450)
        val fadeInBtn = animationUtils.generateFadeInAnimation(500, 450)
        q7.startAnimation(fadeInQ)
        genderToMeetLL.startAnimation(fadeInGender)
        minAge.startAnimation(fadeInMinAge)
        maxAge.startAnimation(fadeInMaxAge)
        toStep8.startAnimation(fadeInBtn)
    }

    private fun seventhStepAnimateViewsOut() {
        val fadeOutQ = animationUtils.generateFadeOutAnimation(500, 0)
        val fadeOutGender = animationUtils.generateFadeOutAnimation(500, 150)
        val fadeOutMinAge = animationUtils.generateFadeOutAnimation(500, 300)
        val fadeOutMaxAge = animationUtils.generateFadeOutAnimation(300, 450)
        val fadeOutBtn = animationUtils.generateFadeOutAnimation(300, 550)
        val fadeOutParent = animationUtils.generateFadeOutAnimation(1, 850)
        q7.startAnimation(fadeOutQ)
        genderToMeetLL.startAnimation(fadeOutGender)
        minAge.startAnimation(fadeOutMinAge)
        maxAge.startAnimation(fadeOutMaxAge)
        toStep8.startAnimation(fadeOutBtn)
        seventhStep.startAnimation(fadeOutParent)

        fadeOutParent.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                genderToMeetLL.visibility = View.GONE
                minAge.visibility = View.GONE
                maxAge.visibility = View.GONE
                seventhStep.visibility = View.GONE
                eighthStepAnimateViewsIn()
            }

            override fun onAnimationStart(p0: Animation?) {

            }

        })
    }

    private fun eighthStepAnimateViewsIn() {
        eighthStep.visibility = View.VISIBLE
        val fadeInQ = animationUtils.generateFadeInAnimation(500, 0)
        val fadeInProfileImage = animationUtils.generateFadeInAnimation(500, 150)
        val fadeInOpenCamera = animationUtils.generateFadeInAnimation(500, 300)
        val fadeInOpenGallery = animationUtils.generateFadeInAnimation(500, 450)
        val fadeInBtn = animationUtils.generateFadeInAnimation(500, 450)
        q8.startAnimation(fadeInQ)
        profileCV.startAnimation(fadeInProfileImage)
        openCamera.startAnimation(fadeInOpenCamera)
        openGallery.startAnimation(fadeInOpenGallery)
        finishSetup.startAnimation(fadeInBtn)
    }

    private fun animateElevation(relativeLayout: RelativeLayout, start: Float, end: Float) {
        val animator = ObjectAnimator.ofFloat(relativeLayout, "elevation", start, end)
        animator.duration = 300
        animator.start()
    }

    private fun animateBackgroundColor(relativeLayout: RelativeLayout, start: Int, end: Int) {
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), start, end)
        colorAnimation.duration = 300
        colorAnimation.addUpdateListener { animator -> relativeLayout.setBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.start()
    }

    private fun bindViews() {
        stepProgress = findViewById(R.id.pb_profile_setup_steps)

        // step 1
        firstStep = findViewById(R.id.rl_step_1)
        q1 = findViewById(R.id.tv_q_1)
        firstName = findViewById(R.id.met_first_name)
        lastName = findViewById(R.id.met_last_name)
        toStep2 = findViewById(R.id.btn_to_step_2)
        q1.visibility = View.INVISIBLE

        // step 2
        secondStep = findViewById(R.id.rl_step_2)
        q2 = findViewById(R.id.tv_q_2)
        bio = findViewById(R.id.met_bio)
        gender = findViewById(R.id.met_gender)
        toStep3 = findViewById(R.id.btn_to_step_3)

        // step 3
        q3 = findViewById(R.id.tv_q_3)
        thirdStep = findViewById(R.id.rl_step_3)
        birthDate = findViewById(R.id.met_birth_date)
        age = findViewById(R.id.met_age)
        toStep4 = findViewById(R.id.btn_to_step_4)
        zodiac = findViewById(R.id.met_zodiac)

        // step 4
        fourthStep = findViewById(R.id.rl_step_4)
        q4 = findViewById(R.id.tv_q_4)
        country = findViewById(R.id.met_country)
        city = findViewById(R.id.met_city)
        toStep5 = findViewById(R.id.btn_to_step_5)

        // step 5
        fifthStep = findViewById(R.id.rl_step_5)
        q5 = findViewById(R.id.tv_q_5)
        student = findViewById(R.id.ll_student)
        yesRL = findViewById(R.id.rl_yes)
        noRL = findViewById(R.id.rl_no)
        yes = findViewById(R.id.tv_yes)
        no = findViewById(R.id.tv_no)
        college = findViewById(R.id.met_college)
        toStep6 = findViewById(R.id.btn_to_step_6)

        // step 6
        sixthStep = findViewById(R.id.rl_step_6)
        q6 = findViewById(R.id.tv_q_6)
        eatTimesRL = findViewById(R.id.rl_eat_times)
        eatTimesSP = findViewById(R.id.sp_eat_times)
        eatTimesTV = findViewById(R.id.tv_eat_times)
        eatTimesEHL = findViewById(R.id.ehl_eat_times)
        toStep7 = findViewById(R.id.btn_to_step_7)

        // step 7
        seventhStep = findViewById(R.id.rl_step_7)
        q7 = findViewById(R.id.tv_q_7)
        minAge = findViewById(R.id.met_min_age)
        maxAge = findViewById(R.id.met_max_age)
        toStep8 = findViewById(R.id.btn_to_step_8)
        genderToMeetLL = findViewById(R.id.ll_gender)
        male = findViewById(R.id.rl_male)
        female = findViewById(R.id.rl_female)

        // step 8
        eighthStep = findViewById(R.id.rl_step_8)
        q8 = findViewById(R.id.tv_q_8)
        profileCV = findViewById(R.id.cv_profile_image)
        profileImage = findViewById(R.id.iv_profile_image)
        openCamera = findViewById(R.id.rl_open_camera)
        openGallery = findViewById(R.id.rl_open_gallery)
        finishSetup = findViewById(R.id.btn_finish_setup)

    }

    private fun showDialog(title :String, message: String) {
        val builder = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setNeutralButton("OK") {dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        builder.create()
        builder.show()
    }

    private fun insertItemsInEatTimes(values: Int) {
        eatTimesList.clear()
        for (index in 0 until values) {
            eatTimesList.add(0)
        }
        eatTimesAdapter.notifyDataSetChanged()
    }

    private fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 21) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (stepProgress.progress == 8) {
            // TODO: user canceled the profile setup => delete profile pictures from db
        }
    }
}
