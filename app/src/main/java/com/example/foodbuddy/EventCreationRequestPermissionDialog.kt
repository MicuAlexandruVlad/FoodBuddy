package com.example.foodbuddy

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.Gson
import org.json.JSONObject

class EventCreationRequestPermissionDialog(
    private val c: Context,
    private var socket: Socket
): Dialog(c) {

    private lateinit var step1: ConstraintLayout
    private lateinit var no: Button
    private lateinit var yes: Button
    private lateinit var step2: ConstraintLayout
    private lateinit var loading: LottieAnimationView
    private lateinit var cancel: Button
    private lateinit var step3: ConstraintLayout
    private lateinit var profileImage: ImageView
    private lateinit var userName: String
    private lateinit var agree: Button
    private lateinit var disagree: Button

    var currentUser = User()
    var foundUser = User()

    // 0 -> normal dialog to send a req
    // 1 -> used for when a req is received
    var dialogType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_event_creation_request_permission)

        bindViews()

        when (dialogType) {
            0 -> {
                step1.visibility = View.VISIBLE
                step2.visibility = View.GONE
                step3.visibility = View.GONE
            }
            1 -> {
                step1.visibility = View.GONE
                step2.visibility = View.GONE
                step3.visibility = View.VISIBLE
                listenForEventCancel()
            }
        }

        no.setOnClickListener {
            dismiss()
        }

        yes.setOnClickListener {
            step1.visibility = View.GONE
            step2.visibility = View.VISIBLE
            val data = JSONObject()
            val gson = Gson()
            data.put("req_user_id", currentUser._id)
            data.put("perm_user_id", foundUser._id)
            data.put("req_user", gson.toJson(currentUser))
            data.put("action", EventRequest.EVENT_REQUEST_PERMISSION)
            socket.emit(SocketEvents.EVENT_REQ_PERMISSION, data)
        }

        cancel.setOnClickListener {
            val data = JSONObject()
            data.put("req_user_id", currentUser._id)
            data.put("perm_user_id", foundUser._id)
            data.put("action", EventRequest.EVENT_CANCEL_PERMISSION_REQUEST)
            socket.emit(SocketEvents.EVENT_CANCEL_REQ_PERMISSION, data)
            dismiss()
        }

        agree.setOnClickListener {
            val data = JSONObject()
            data.put("req_user_id", currentUser._id)
            data.put("perm_user_id", foundUser._id)
            data.put("action", EventRequest.EVENT_PERMISSION_GRANTED)
            socket.emit(SocketEvents.EVENT_PERMISSION_HANDLE, data)
            dismiss()
        }

        disagree.setOnClickListener {
            val data = JSONObject()
            data.put("req_user_id", currentUser._id)
            data.put("perm_user_id", foundUser._id)
            data.put("action", EventRequest.EVENT_PERMISSION_DENIED)
            socket.emit(SocketEvents.EVENT_PERMISSION_HANDLE, data)
            dismiss()
        }
    }

    private fun bindViews() {
        step1 = findViewById(R.id.cl_step_1)
        no = findViewById(R.id.btn_no)
        yes = findViewById(R.id.btn_yes)
        step2 = findViewById(R.id.cl_step_2)
        loading = findViewById(R.id.l_loading)
        cancel = findViewById(R.id.btn_cancel)
        step3 = findViewById(R.id.cl_step_3)
        profileImage = findViewById(R.id.iv_profile_image)
        agree = findViewById(R.id.btn_y)
        disagree = findViewById(R.id.btn_n)
    }

    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        super.setOnDismissListener(listener)
        socket.disconnect()
    }

    private fun listenForEventCancel() {
        LocalBroadcastManager.getInstance(c).registerReceiver(object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            override fun onReceive(context: Context, intent: Intent) {
                val eventRequest = intent.getSerializableExtra("event-request") as EventRequest
                if (eventRequest.reqType == EventRequest.EVENT_CANCEL_PERMISSION_REQUEST) {
                    Toast.makeText(c, "User canceled the request", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }, IntentFilter("event-request"))
    }
}