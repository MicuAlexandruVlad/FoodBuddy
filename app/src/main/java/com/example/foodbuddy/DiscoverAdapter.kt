package com.example.foodbuddy

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide

class DiscoverAdapter(private var items: ArrayList<User>,
                      private var context: Context?
                      ) : RecyclerView.Adapter<DiscoverAdapter.ViewHolder>() {
    var profilePics: ArrayList<String> = ArrayList()
    private val TAG = "DiscoverAdapter"

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.discover_list_item, p0, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val foundUser = items[position]

        holder.zodiac.text = foundUser.zodiac.capitalize()
        holder.userName.text = foundUser.firstName + " " + foundUser.lastName
        holder.eatTimePeriods.text = foundUser.eatTimePeriods
        holder.userAge.text = foundUser.age.toString() + " years"

        context?.let { Glide.with(it).load(stringToBitmap(profilePics[position])).centerCrop().into(holder.profileImage) }

        holder.sendMessage.setOnClickListener {
            // TODO: launch chat activity
        }
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val zodiac: TextView = view.findViewById(R.id.tv_zodiac)
        val sendMessage: RelativeLayout = view.findViewById(R.id.rl_send_message)
        val userName: TextView = view.findViewById(R.id.tv_user_name)
        val userAge: TextView = view.findViewById(R.id.tv_age)
        val eatTimePeriods: TextView = view.findViewById(R.id.tv_eat_time_periods)
        val profileImage: ImageView = view.findViewById(R.id.iv_profile_image)
    }

    private fun stringToBitmap(data: String): Bitmap {

        //TODO: try to upload the image without encoding too Base64
        val bytes = Base64.decode(data, Base64.DEFAULT)
        Log.d(TAG, "byte array size -> " + bytes.size)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}