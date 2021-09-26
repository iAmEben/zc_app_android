package com.tolstoy.zurichat.ui.fragments.home_screen.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tolstoy.zurichat.databinding.ItemChatBinding
import com.tolstoy.zurichat.models.Message

class ChatsAdapter(private val chats: List<Chat>): RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    private var onItemClickListener: ((Chat) -> Unit)? = null

    fun setItemClickListener(listener: (Chat) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(ItemChatBinding
            .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = chats.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) = holder.bind(chats[position])

    inner class ChatViewHolder(private val binding: ItemChatBinding):
        RecyclerView.ViewHolder(binding.root){

        fun bind(chat: Chat) = with(binding){
            textChatLastMessage.text = chat.message.message
            textChatTime.text = chat.message.createdAt
            textChatUsername.text = chat.sender
            textChatUnreadMessages.text = chat.unreadCount.toString()

            root.setOnClickListener{
                onItemClickListener?.invoke(chat)
            }
        }
    }

    data class Chat(val sender: String, val message: Message, val unreadCount: Int)
}