package service.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import service.firebase.model.UserData

class UserDataRepositoryDelegate private constructor() {
    private val database: FirebaseFirestore = Firebase.firestore

    fun add(userData: UserData): Task<Void> {
        return database
            .collection(COLLECTION_NAME)
            .document(userData.email)
            .set(userData.toMap())
    }

    fun add(
        email: String,
        nickname: String,
        birthday: String
    ): Task<Void> {
        return add(UserData(email, nickname, birthday))
    }

    fun get(email: String): Task<DocumentSnapshot> {
        return database
            .collection(COLLECTION_NAME)
            .document(email)
            .get()
    }

    fun getDocumentReference(email: String): DocumentReference {
        return database
            .collection(COLLECTION_NAME)
            .document(email)
    }

    companion object {
        private const val COLLECTION_NAME = "user"

        val repository: UserDataRepositoryDelegate =
            UserDataRepositoryDelegate()
    }
}