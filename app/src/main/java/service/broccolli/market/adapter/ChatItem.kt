package service.broccolli.market.adapter

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import service.broccolli.market.R
import service.firebase.UserDataRepositoryDelegate
import service.firebase.auth.FirebaseAuthDelegate
import service.firebase.model.ChatData
import service.firebase.model.UserData
import java.time.Instant
import java.util.Date

class ChatItem(
    private val activity: Activity,
    view: View
) : RecyclerView.ViewHolder(view) {
    private var senderView: TextView = view.findViewById(R.id.chat_sender)
    private var timeView: TextView = view.findViewById(R.id.chat_time)
    private var chatContentView: TextView = view.findViewById(R.id.chat_content)

    fun setContents(chatData: ChatData) {
        initialize(chatData)
    }

    private fun initialize(chatData: ChatData) {
        val target =
            if (chatData.sender == FirebaseAuthDelegate.currentUser?.email) {
                chatData.receiver
            } else {
                chatData.sender
            }
        UserDataRepositoryDelegate.repository
            .get(target)
            .addOnSuccessListener {
                val userData = UserData(it)
                activity.runOnUiThread {
                    senderView.text =
                        if (chatData.sender == FirebaseAuthDelegate.currentUser?.email) {
                            "${userData.nickname}에게 보낸 쪽지"
                        } else {
                            "${userData.nickname}가 보낸 쪽지"
                        }
                    timeView.text =
                        ChatData.formatDate(
                            Date.from(Instant.now()),
                            chatData.timestamp
                        )
                    chatContentView.text = chatData.content
                }
            }
    }
}