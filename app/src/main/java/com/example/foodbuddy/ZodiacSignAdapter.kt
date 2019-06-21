package com.example.foodbuddy

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide

class ZodiacSignAdapter(private var items: ArrayList<ZodiacSign>,
                        private var context: Context?,
                        private var hideCheckBox: Boolean
                      ) : RecyclerView.Adapter<ZodiacSignAdapter.ViewHolder>() {
    private val TAG = "DiscoverAdapter"

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.zodiac_list_item, p0, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val zodiacSign = items[position]

        holder.zodiacName.text = zodiacSign.zodiacName.toUpperCase()
        holder.zodiacImage.setImageDrawable(getSignDrawable(zodiacSign.zodiacName, context!!))
        holder.selection.isChecked = zodiacSign.selected

        if (hideCheckBox) {
            holder.selection.visibility = View.GONE
        }
        else {
            holder.body.setOnClickListener {
                if (zodiacSign.selected) {
                    zodiacSign.selected = false
                    holder.selection.isChecked = false
                }
                else {
                    zodiacSign.selected = true
                    holder.selection.isChecked = true
                }
            }

            holder.selection.setOnClickListener {
                if (zodiacSign.selected) {
                    zodiacSign.selected = false
                    holder.selection.isChecked = false
                }
                else {
                    zodiacSign.selected = true
                    holder.selection.isChecked = true
                }
            }
        }
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val zodiacImage: ImageView = view.findViewById(R.id.iv_zodiac_sign)
        val zodiacName: TextView = view.findViewById(R.id.tv_zodiac_sign_name)
        val body: RelativeLayout = view.findViewById(R.id.rl_zodiac_sign_body)
        val selection: CheckBox = view.findViewById(R.id.cb_zodiac_sign)
    }

    private fun getSignDrawable(name: String, context: Context): Drawable {
        return when (name) {
            "virgo" -> {
                ContextCompat.getDrawable(context, R.drawable.virgo)
            }
            "aquarius" -> {
                ContextCompat.getDrawable(context, R.drawable.aquarius)
            }
            "aries" -> {
                ContextCompat.getDrawable(context, R.drawable.aries)
            }
            "cancer" -> {
                ContextCompat.getDrawable(context, R.drawable.cancer)
            }
            "capricorn" -> {
                ContextCompat.getDrawable(context, R.drawable.capricorn)
            }
            "gemini" -> {
                ContextCompat.getDrawable(context, R.drawable.gemini)
            }
            "leo" -> {
                ContextCompat.getDrawable(context, R.drawable.leo)
            }
            "libra" -> {
                ContextCompat.getDrawable(context, R.drawable.libra)
            }
            "pisces" -> {
                ContextCompat.getDrawable(context, R.drawable.pisces)
            }
            "sagittarius" -> {
                ContextCompat.getDrawable(context, R.drawable.sagittarius)
            }
            "scorpio" -> {
                ContextCompat.getDrawable(context, R.drawable.scorpio)
            }
            else -> ContextCompat.getDrawable(context, R.drawable.taurus)
        }!!
    }
}