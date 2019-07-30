package com.example.foodbuddy

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide

class ConversationAdapter(private var items: ArrayList<Conversation>,
                          private var context: Context?,
                          private var currentUser: User
                      ) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {
    val dbLinks = DBLinks()
    private val TAG = "DiscoverAdapter"

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.conversation_list_item, p0, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = items[position]

        context?.let { Glide.with(it).load(dbLinks
            .getImageSmall(conversation.conversationId, conversation.profilePhotoId)).into(holder.profileImage) }

        holder.userName.text = conversation.conversationUser.firstName + " " +
                conversation.conversationUser.lastName
        holder.timestamp.text = formatTime(conversation.lastMessage.timestamp)
        holder.lastMessageText.text = conversation.lastMessage.messageText
        holder.body.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("fromConversationAdapter", true)
            intent.putExtra("conversation", conversation)
            intent.putExtra("currentUser", currentUser)
            context!!.startActivity(intent)
        }

        if (conversation.unreadMessages == 0)
            holder.newMessagesHolder.visibility = View.INVISIBLE
        else {
            holder.newMessagesHolder.visibility = View.VISIBLE
            holder.newMessages.text = conversation.unreadMessages.toString()
        }
        if (conversation.lastMessage.read &&
            conversation.lastMessage.senderId.compareTo(currentUser._id, false) == 0)
            holder.seen.visibility = View.VISIBLE
        else
            holder.seen.visibility = View.GONE

    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.iv_profile_image)
        val seen: ImageView = view.findViewById(R.id.iv_seen)
        val userName: TextView = view.findViewById(R.id.tv_user_name)
        val timestamp: TextView = view.findViewById(R.id.tv_timestamp)
        val lastMessageText: TextView = view.findViewById(R.id.tv_message_text)
        val newMessages: TextView = view.findViewById(R.id.tv_new_messages)
        val newMessagesHolder: RelativeLayout = view.findViewById(R.id.rl_new_messages)
        val body: RelativeLayout = view.findViewById(R.id.rl_body)
    }

    private fun formatTime(time: String): String {
        return time.split(" ")[3].split(":")[0] + " : " +
                time.split(" ")[3].split(":")[1]
    }
}