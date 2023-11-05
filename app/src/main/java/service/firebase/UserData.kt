package service.firebase

data class UserData(
    val email: String,
    val nickname: String,
    val birthday: String
) {
    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "email" to email,
            "nickname" to nickname,
            "birthday" to birthday
        )
    }
}
