package service.broccolli.market

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import service.broccolli.util.EditTextUtil.Companion.addContentErrorMessageChecker
import service.firebase.ArticleDataRepositoryDelegate
import service.firebase.auth.FirebaseAuthDelegate
import java.util.Date

class ArticlePublishActivity : AppCompatActivity() {
    private lateinit var submitButton: FloatingActionButton
    private lateinit var titleEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var contentEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_publish)
        initialize()

        val articleId = intent.getStringExtra("articleId")

        if (articleId != null) {
            titleEditText.setText(intent.getStringExtra("title")!!)
            priceEditText.setText(intent.getIntExtra("price", 0).toString())
            contentEditText.setText(intent.getStringExtra("content")!!)
        }

        titleEditText.addContentErrorMessageChecker(ArticlePublishActivity::getTitleErrorMessage)
        priceEditText.addContentErrorMessageChecker(ArticlePublishActivity::getPriceErrorMessage)
        contentEditText.addContentErrorMessageChecker(ArticlePublishActivity::getContentErrorMessage)
        submitButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val price = priceEditText.text.toString()
            val content = contentEditText.text.toString()
            if (getTitleErrorMessage(title) != null) {
                return@setOnClickListener
            }
            if (getPriceErrorMessage(price) != null) {
                return@setOnClickListener
            }
            if (getContentErrorMessage(content) != null) {
                return@setOnClickListener
            }
            val timestamp = Date()
            if (articleId == null) {
                ArticleDataRepositoryDelegate.repository
                    .add(
                        title,
                        FirebaseAuthDelegate.currentUser!!.email!!,
                        content,
                        price.toInt(),
                        false,
                        timestamp
                    ).addOnSuccessListener {
                        setResult(
                            RESULT_OK,
                            Intent().putExtra("intent", "articlePublish")
                        )
                        finish()
                    }
                return@setOnClickListener
            }
            ArticleDataRepositoryDelegate.repository
                .update(
                    articleId,
                    title,
                    FirebaseAuthDelegate.currentUser!!.email!!,
                    content,
                    price.toInt(),
                    false,
                    timestamp
                ).addOnSuccessListener {
                    setResult(
                        RESULT_OK,
                        Intent().putExtra("intent", "articleEdit")
                    )
                    finish()
                }
        }
    }

    private fun initialize() {
        submitButton = findViewById(R.id.article_publish_submit_button)
        titleEditText = findViewById(R.id.article_publish_title_edit_text)
        priceEditText = findViewById(R.id.article_publish_price_edit_text)
        contentEditText = findViewById(R.id.article_publish_content_edit_text)
    }

    companion object {
        private fun getTitleErrorMessage(content: String): String? {
            if (content.trim().isEmpty()) {
                return "제목을 입력해 주세요."
            }
            return null
        }

        private fun getPriceErrorMessage(content: String): String? {
            if (content.trim().isEmpty()) {
                return "가격을 입력해 주세요."
            }
            if (content.toUIntOrNull() == null) {
                return "정확하지 않은 입력입니다."
            }
            return null
        }

        private fun getContentErrorMessage(content: String): String? {
            if (content.trim().isEmpty()) {
                return "내용을 입력해 주세요."
            }
            return null
        }
    }
}