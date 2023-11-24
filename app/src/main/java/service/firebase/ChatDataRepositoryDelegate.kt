package service.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import service.firebase.model.ChatData
import java.util.Date

class ChatDataRepositoryDelegate private constructor() {
    private val database: FirebaseFirestore = Firebase.firestore

    suspend fun get(
        userEmail: String,
        limit: Int = 30,
        before: Date? = null
    ): List<ChatData> {
        var query = database
            .collection(COLLECTION_NAME)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .where(
                Filter.or(
                    Filter.equalTo("sender", userEmail),
                    Filter.equalTo("receiver", userEmail)
                )
            )
        if (before != null) {
            query = query.whereLessThan("timestamp", Timestamp(before))
        }
        return query
            .limit(limit.toLong())
            .get()
            .await()
            .map { ChatData(it) }
    }

    fun sendChat(
        sender: String,
        receiver: String,
        content: String,
        timestamp: Date
    ): Task<Void> {
        return database
            .collection(COLLECTION_NAME)
            .document()
            .set(ChatData(sender, receiver, content, timestamp))
    }

    companion object {
        private const val COLLECTION_NAME = "chats"

        val repository: ChatDataRepositoryDelegate =
            ChatDataRepositoryDelegate()
    }
}