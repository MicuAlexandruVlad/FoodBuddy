package com.example.foodbuddy

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Button
import com.daimajia.easing.linear.Linear

class PickZodiacSignDialog(context: Context) : Dialog(context) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cancel: Button
    private lateinit var done: Button

    private lateinit var adapter: ZodiacSignAdapter
    private lateinit var items: ArrayList<ZodiacSign>
    private lateinit var selectedItems: ArrayList<ZodiacSign>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_pick_zodiac_sign)

        bindViews()

        items = ArrayList()
        selectedItems = ArrayList()
        val signs = arrayOf("aquarius", "aries", "cancer", "capricorn", "gemini", "leo", "libra",
            "pisces", "sagittarius", "scorpio", "taurus", "virgo")
        initItems(signs)

        adapter = ZodiacSignAdapter(items, context, false)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        cancel.setOnClickListener {
            dismiss()
        }

        done.setOnClickListener {
            dismiss()
        }
    }

    private fun initItems(signs: Array<String>) {
        for (index in 0 until signs.size) {
            val zodiacSign = ZodiacSign()
            zodiacSign.selected = false
            zodiacSign.zodiacName = signs[index]
            items.add(zodiacSign)
        }
    }

    private fun bindViews() {
        recyclerView = findViewById(R.id.rv_zodiac_signs)
        cancel = findViewById(R.id.btn_cancel)
        done = findViewById(R.id.btn_done)
    }

    fun getSelectedItems(): ArrayList<ZodiacSign> {
        for (index in 0 until items.size) {
            val zodiacSign = items[index]
            if (zodiacSign.selected)
                selectedItems.add(zodiacSign)
        }
        return selectedItems
    }

}