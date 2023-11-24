package service.firebase.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

data class ChatData(
    val sender: String,
    val receiver: String,
    val content: String,
    val timestamp: Date
) {
    constructor(snapshot: DocumentSnapshot) : this(
        snapshot.getString("sender")!!,
        snapshot.getString("receiver")!!,
        snapshot.getString("content")!!,
        snapshot.getDate("timestamp")!!
    )

    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "sender" to sender,
            "receiver" to receiver,
            "content" to content,
            "timestamp" to FieldValue.serverTimestamp()
        )
    }

    companion object {
        fun buildMapOf(
            sender: String,
            receiver: String,
            content: String,
            timestamp: Date
        ): HashMap<String, Any> {
            return hashMapOf(
                "sender" to sender,
                "receiver" to receiver,
                "content" to content,
                "timestamp" to Timestamp(timestamp)
            )
        }

        fun formatDate(now: Date, date: Date): String {
            val dateLocal = date.toInstant()
                .atZone(ZoneId.systemDefault())
            val nowLocal = now.toInstant()
                .atZone(ZoneId.systemDefault())
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
    }
}