package service.firebase

import com.google.firebase.firestore.DocumentSnapshot

data class UserData(
    val email: String,
    val nickname: String,
    val birthday: String
) {
    constructor(snapshot: DocumentSnapshot) : this(
        snapshot.getString("email")!!,
        snapshot.getString("nickname")!!,
        snapshot.getString("birthday")!!
    )

    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "email" to email,
            "nickname" to nickname,
            "birthday" to birthday
        )
    }
}
