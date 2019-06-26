package com.example.foodbuddy

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

class MessageAdapter (private var messages: ArrayList<Message>,
                      private var context: Context,
                      private var currentUser: User) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val dbLinks = DBLinks()

    override fun getItemViewType(position: Int): Int {
        if (messages[position].senderId.compareTo(currentUser._id, false) == 0)
            return 0
        return 1
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val sentMessageView = LayoutInflater.from(p0.context).
            inflate(R.layout.sent_text_message_list_item, p0, false)
        val receivedMessageView = LayoutInflater.from(p0.context).
            inflate(R.layout.received_text_message_list_item, p0, false)


        if (p1 == 0)
            return SentMessageViewHolder(sentMessageView)
        return ReceivedMessageViewHolder(receivedMessageView)
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        val message = messages[p1]

        when (p0.itemViewType) {
            0 -> {
                val holder = p0 as SentMessageViewHolder
                holder.messageText.text = message.messageText
                holder.timestamp.text = formatTime(message.timestamp)
            }

            1 -> {
                val holder = p0 as ReceivedMessageViewHolder
                holder.messageText.text = message.messageText
                holder.timestamp.text = formatTime(message.timestamp)
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class SentMessageViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.tv_message_text)
        val timestamp: TextView = view.findViewById(R.id.tv_timestamp)
        val body: RelativeLayout = view.findViewById(R.id.rl_message_body)
    }

    class ReceivedMessageViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.tv_message_text)
        val timestamp: TextView = view.findViewById(R.id.tv_timestamp)
        val body: LinearLayout = view.findViewById(R.id.ll_message_body)
    }

    private fun formatTime(time: String): String {
        return time.split(" ")[3].split(":")[0] + " : " +
                time.split(" ")[3].split(":")[1]
    }
}