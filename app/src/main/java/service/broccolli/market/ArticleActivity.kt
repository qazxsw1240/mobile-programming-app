package service.broccolli.market

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import service.firebase.ArticleData
import service.firebase.ArticleDataRepositoryDelegate
import service.firebase.UserData
import service.firebase.UserDataRepositoryDelegate
import service.firebase.auth.FirebaseAuthDelegate
import java.time.Instant
import java.util.Date

class ArticleActivity : AppCompatActivity() {
    private lateinit var articleTitleTextView: TextView
    private lateinit var articleAuthorTextView: TextView
    private lateinit var articleDateTextView: TextView
    private lateinit var articleContentTextView: TextView
    private lateinit var articleBackButton: Button
    private lateinit var articleDeleteButton: Button
    private lateinit var articleActionButton: FloatingActionButton

    private lateinit var articleData: ArticleData
    private lateinit var userData: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)
        initialize()

        val articleId = intent.getStringExtra("articleId")

        if (articleId == null) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        CoroutineScope(Dispatchers.Default).launch {
            val snapshot = ArticleDataRepositoryDelegate
                .repository
                .collection
                .document(articleId)
                .get()
                .await()
            articleData = ArticleData(snapshot)
            val author =
                UserDataRepositoryDelegate.repository
                    .get(articleData.authorEmail)
                    .await()
            userData = UserData(author)
            runOnUiThread {
                articleTitleTextView.text = articleData.title
                articleAuthorTextView.text = userData.nickname
                articleDateTextView.text = ArticleData.formatDate(
                    Date.from(Instant.now()),
                    articleData.uploadTime
                )
                articleContentTextView.text = articleData.content
                if (userData.email == articleData.authorEmail) {
                    articleDeleteButton.isVisible = true
                }
            }
        }

        articleBackButton.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        articleDeleteButton.setOnClickListener {
            ArticleDataRepositoryDelegate.repository.collection
                .document(articleId)
                .delete()
                .addOnSuccessListener {
                    setResult(
                        RESULT_OK,
                        Intent()
                            .putExtra("intent", "articleDelete")
                            .putExtra("articleId", articleId)
                    )
                    finish()
                }
        }

        articleActionButton.setOnClickListener {
            // edit mode
            if (userData.email == FirebaseAuthDelegate.currentUser?.email) {
                val intent = Intent(
                    this@ArticleActivity,
                    ArticlePublishActivity::class.java
                )
                intent.putExtra("articleId", articleId)
                intent.putExtra("title", articleData.title)
                intent.putExtra("price", articleData.price)
                intent.putExtra("content", articleData.content)
                startActivity(intent)
                return@setOnClickListener
            }
            // start chat
        }
    }

    private fun initialize() {
        articleTitleTextView = findViewById(R.id.article_title)
        articleAuthorTextView = findViewById(R.id.article_author)
        articleDateTextView = findViewById(R.id.article_date)
        articleContentTextView = findViewById(R.id.article_content)
        articleBackButton = findViewById(R.id.article_button_back)
        articleDeleteButton = findViewById(R.id.article_delete_button)
        articleActionButton = findViewById(R.id.article_action_button)
    }
}