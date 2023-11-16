package service.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import service.firebase.model.ArticleData
import java.util.Date

class ArticleDataRepositoryDelegate private constructor() {
    private val database: FirebaseFirestore = Firebase.firestore

    val collection: CollectionReference
        get() = database.collection(COLLECTION_NAME)

    fun add(
        title: String,
        authorEmail: String,
        content: String,
        price: Int,
        isResolved: Boolean,
        uploadTime: Date
    ): Task<Void> {
        return database
            .collection(COLLECTION_NAME)
            .document()
            .set(
                ArticleData.buildMapOf(
                    title,
                    authorEmail,
                    content,
                    price,
                    isResolved,
                    uploadTime
                )
            )
    }

    fun update(
        id: String,
        title: String,
        authorEmail: String,
        content: String,
        price: Int,
        isResolved: Boolean,
        uploadTime: Date
    ): Task<Void> {
        return database
            .collection(COLLECTION_NAME)
            .document(id)
            .set(
                ArticleData.buildMapOf(
                    title,
                    authorEmail,
                    content,
                    price,
                    isResolved,
                    uploadTime
                )
            )
    }

    fun add(articleData: ArticleData): Task<Void> {
        return database
            .collection(COLLECTION_NAME)
            .document()
            .set(articleData.toMap())
    }

    suspend fun get(): List<ArticleData> {
        return database
            .collection(COLLECTION_NAME)
            .get()
            .await()
            .map { ArticleData(it) }
    }

    suspend fun get(filter: Filter): List<ArticleData> {
        return database
            .collection(COLLECTION_NAME)
            .where(filter)
            .get()
            .await()
            .map { ArticleData(it) }
    }

    suspend fun get(order: String, filter: Filter): List<ArticleData> {
        return database
            .collection(COLLECTION_NAME)
            .orderBy(order)
            .where(filter)
            .get()
            .await()
            .map { ArticleData(it) }
    }

    companion object {
        private const val COLLECTION_NAME = "articles"

        val repository: ArticleDataRepositoryDelegate =
            ArticleDataRepositoryDelegate()
    }
}