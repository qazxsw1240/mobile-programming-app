package service.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.Date

data class ArticleData(
    val title: String,
    val authorEmail: String,
    val content: String,
    val price: Int,
    val isResolved: Boolean,
    val uploadTime: Date
) {
    constructor(document: QueryDocumentSnapshot) : this(
        document.getString("title")!!,
        document.getString("author")!!,
        document.getString("content")!!,
        document.getLong("price")!!.toInt(),
        document.getBoolean("isResolved")!!,
        document.getTimestamp("uploadTime")!!.toDate()
    )

    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "title" to title,
            "author" to authorEmail,
            "content" to content,
            "price" to price,
            "isResolved" to isResolved,
            "uploadTime" to Timestamp(uploadTime)
        )
    }
}