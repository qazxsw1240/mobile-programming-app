package service.firebase.model

import com.google.firebase.firestore.DocumentSnapshot

data class UserData(
    val email: String,
    val nickname: String,
    val birthday: String,
    val chats: List<String> = listOf()
) {
    constructor(snapshot: DocumentSnapshot) : this(
        snapshot.getString("email")!!,
        snapshot.getString("nickname")!!,
        snapshot.getString("birthday")!!,
        snapshot.get("chats")!! as List<String>
    )

    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "email" to email,
            "nickname" to nickname,
            "birthday" to birthday,
            "chats" to chats
        )
    }
}
