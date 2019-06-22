package com.example.foodbuddy

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
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
                      private var context: Context?,
                      private var currentUser: User
                      ) : RecyclerView.Adapter<DiscoverAdapter.ViewHolder>() {
    var userImages: ArrayList<UserImage> = ArrayList()
    val dbLinks = DBLinks()
    val MESSAGE_ACTIVITY = 123
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




        context?.let { Glide.with(it).load(dbLinks.getImageSmall(foundUser._id ,foundUser.profileImageId))
            .centerCrop().into(holder.profileImage) }

        holder.sendMessage.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("currentUser", currentUser)
            intent.putExtra("foundUser", foundUser)
            (context as Activity).startActivityForResult(intent, MESSAGE_ACTIVITY)
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
}