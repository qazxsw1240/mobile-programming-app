package service.broccolli.market.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import service.broccolli.market.R
import service.firebase.auth.FirebaseAuthDelegate
import service.firebase.model.ChatData

class ChatItemAdapter(
    private val activity: Activity,
    private val chatItems: MutableList<ChatData>,
    private val listener: (chatItem: ChatData) -> Unit
) :
    RecyclerView.Adapter<ChatItem>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatItem {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view =
            layoutInflater.inflate(R.layout.chat_item, parent, false)
        val viewHolder = ChatItem(activity, view)
        view.setOnLongClickListener {
            val chatItem = chatItems[viewHolder.layoutPosition]
            if (chatItem.sender == FirebaseAuthDelegate.currentUser?.email) {
                return@setOnLongClickListener true
            }
            listener(chatItem)
            return@setOnLongClickListener true
        }
        return viewHolder
    }

    override fun getItemCount(): Int = chatItems.size

    override fun onBindViewHolder(holder: ChatItem, position: Int) {
        holder.setContents(chatItems[position])
    }

    fun getItemList(): List<ChatData> = chatItems

    fun clear() = this.chatItems.clear()

    fun fetchItems(newChatItems: MutableList<ChatData>) {
        chatItems.addAll(newChatItems)
    }
}