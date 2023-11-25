package service.firebase.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

data class ArticleData(
    val id: String,
    val title: String,
    val authorEmail: String,
    val content: String,
    val price: Int,
    val isResolved: Boolean,
    val uploadTime: Date
) {
    constructor(snapshot: DocumentSnapshot) : this(
        snapshot.id,
        snapshot.getString("title")!!,
        snapshot.getString("author")!!,
        snapshot.getString("content")!!,
        snapshot.getLong("price")!!.toInt(),
        snapshot.getBoolean("isResolved")!!,
        snapshot.getTimestamp("uploadTime")!!.toDate()
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

    companion object {
        fun buildMapOf(
            title: String,
            authorEmail: String,
            content: String,
            price: Int,
            isResolved: Boolean,
            uploadTime: Date
        ): HashMap<String, Any> {
            return hashMapOf(
                "title" to title,
                "author" to authorEmail,
                "content" to content,
                "price" to price,
                "isResolved" to isResolved,
                "uploadTime" to Timestamp(uploadTime)
            )
        }

        fun formatDate(now: Instant, date: Instant): String {
            val dateLocal = date.atZone(ZoneId.systemDefault())
            val nowLocal = now.atZone(ZoneId.systemDefault())
            val distance =
                Period.between(dateLocal.toLocalDate(), nowLocal.toLocalDate())
            if (distance.days < 1) {
                return dateLocal.format(DateTimeFormatter.ofPattern("오늘 HH:mm"))
            }
            if (distance.days == 1) {
                return dateLocal.format(DateTimeFormatter.ofPattern("어제 HH:mm"))
            }
            if (distance.years < 1) {
                return dateLocal.format(DateTimeFormatter.ofPattern("M월 d일 HH:mm"))
            }
            return dateLocal.format(DateTimeFormatter.ofPattern("yyyy년 M월d일 HH:mm"))
        }

        fun formatDate(now: Date, date: Date): String =
            formatDate(now.toInstant(), date.toInstant())
    }
}