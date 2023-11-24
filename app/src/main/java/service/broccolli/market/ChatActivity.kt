package service.broccolli.market

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import service.broccolli.market.adapter.ChatItemAdapter
import service.broccolli.market.fragment.ChatSendDialog
import service.firebase.ChatDataRepositoryDelegate
import service.firebase.auth.FirebaseAuthDelegate
import service.firebase.model.ChatData
import java.time.Instant
import java.util.Date
import kotlin.math.min

class ChatActivity : AppCompatActivity(), ChatSendDialog.ChatDialogListener {
    private lateinit var refreshButton: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: ChatItemAdapter

    private var currentChatReceiverHolder: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        initialize()
        prepareChats()

        refreshButton.setOnClickListener {
            prepareChats()
        }

        // auto-fetch on scroll
        recyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                if (!recyclerView.canScrollVertically(1)) {
                    fetchArticles()
                }
            }
        })
    }

    private fun initialize() {
        refreshButton = findViewById(R.id.activity_chat_refresh_button)
        recyclerView = findViewById(R.id.activity_chat_view)
        recyclerAdapter =
            ChatItemAdapter(this, mutableListOf(), this::handleDialog)
        recyclerView.layoutManager = RecyclerViewWrapperLayoutManager(this)
        recyclerView.adapter = recyclerAdapter
    }

    private fun prepareChats() {
        val currentUser = FirebaseAuthDelegate.currentUser!!
        ChatDataRepositoryDelegate.repository
            .get(currentUser.email!!)
            .addOnSuccessListener { it ->
                val chats = it
                    .map { ChatData(it) }
                    .toMutableList()
                attachChats(chats)
            }
            .addOnFailureListener {
                Log.e("BroccoliMarket", it.message.toString())
            }
    }

    private fun attachChats(
        chatDataList: MutableList<ChatData>,
        replace: Boolean = true
    ) {
        val maxCount = 30
        if (replace) {
            recyclerAdapter.clear()
        }
        val currentItemCount = recyclerAdapter.itemCount
        recyclerAdapter.fetchItems(chatDataList)
        val newItemCount = recyclerAdapter.itemCount
        val attachedItemCount = newItemCount - currentItemCount
        if (attachedItemCount == 0) {
            return
        }
        recyclerAdapter.notifyItemRangeInserted(
            currentItemCount,
            min(maxCount, newItemCount - currentItemCount)
        )
        recyclerView.smoothScrollToPosition(0)
    }

    private fun fetchArticles() {
        val list = recyclerAdapter.getItemList()
        val lastItem = list.lastOrNull()
        if (lastItem == null) {
            prepareChats()
            return
        }
        val maxCount = 10
        ChatDataRepositoryDelegate.repository
            .get(
                FirebaseAuthDelegate.currentUser?.email!!,
                maxCount,
                lastItem.timestamp
            )
            .addOnSuccessListener { it ->
                val chats = it
                    .map { ChatData(it) }
                    .toMutableList()
                attachChats(chats, false)
            }
            .addOnFailureListener {
                Log.e("BroccoliMarket", it.message.toString())
            }

    }

    private fun handleDialog(chatItem: ChatData) {
        currentChatReceiverHolder = chatItem.sender
        ChatSendDialog()
            .show(supportFragmentManager, "chatSendDialog")
    }

    override fun onPositiveButtonClickListener(dialog: ChatSendDialog) {
        if (currentChatReceiverHolder == null) {
            return
        }
        ChatDataRepositoryDelegate.repository
            .sendChat(
                FirebaseAuthDelegate.currentUser?.email!!,
                currentChatReceiverHolder!!,
                dialog.content,
                Date.from(Instant.now())
            )
            .addOnSuccessListener {
                prepareChats()
            }
            .addOnFailureListener {
                Log.e("BroccoliMarket", it.message.toString())
            }
        currentChatReceiverHolder = null
    }

    override fun onNegativeButtonClickListener(dialog: ChatSendDialog) {
        currentChatReceiverHolder = null
    }

    // https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in
    private class RecyclerViewWrapperLayoutManager(context: Context) :
        LinearLayoutManager(context) {
        override fun onLayoutChildren(
            recycler: RecyclerView.Recycler?,
            state: RecyclerView.State?
        ) {
            try {
                super.onLayoutChildren(recycler, state)
            } catch (e: IndexOutOfBoundsException) {
                Log.e("TAG", "meet a IOOBE in RecyclerView")
            }
        }
    }
}