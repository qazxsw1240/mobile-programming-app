package service.broccolli.market

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import service.broccolli.market.fragment.ChatSendDialog
import service.firebase.ArticleDataRepositoryDelegate
import service.firebase.ChatDataRepositoryDelegate
import service.firebase.UserDataRepositoryDelegate
import service.firebase.auth.FirebaseAuthDelegate
import service.firebase.model.ArticleData
import service.firebase.model.UserData
import java.time.Instant
import java.util.Date

class ArticleActivity : AppCompatActivity(), ChatSendDialog.ChatDialogListener {
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var articleTitleTextView: TextView
    private lateinit var articleAuthorTextView: TextView
    private lateinit var articleDateTextView: TextView
    private lateinit var articlePriceTextView: TextView
    private lateinit var articleContentTextView: TextView
    private lateinit var articleBackButton: Button
    private lateinit var articleDeleteButton: Button
    private lateinit var articleResolveButton: Button
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

        setContents(articleId)

        articleBackButton.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        articleDeleteButton.setOnClickListener {
            AlertDialog.Builder(this@ArticleActivity)
                .setMessage("삭제하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
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
                .setNegativeButton("아니요") { dialog, _ ->
                    dialog.cancel()
                }
                .create()
                .show()
        }

        articleResolveButton.setOnClickListener {
            AlertDialog.Builder(this@ArticleActivity)
                .setMessage("판매를 완료하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    ArticleDataRepositoryDelegate.repository
                        .update(
                            articleId,
                            articleData.title,
                            articleData.authorEmail,
                            articleData.content,
                            articleData.price,
                            true,
                            articleData.uploadTime
                        )
                        .addOnSuccessListener {
                            setContents(articleId)
                        }
                }
                .setNegativeButton("아니요") { dialog, _ ->
                    dialog.cancel()
                }
                .create()
                .show()
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
                activityResultLauncher.launch(intent)
                return@setOnClickListener
            }
            // start chat
            ChatSendDialog()
                .show(
                    supportFragmentManager,
                    "chatSendDialog"
                )
        }
    }

    private fun initialize() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != RESULT_OK) {
                    return@registerForActivityResult
                }
                val articleId = intent.getStringExtra("articleId")
                if (articleId == null) {
                    Log.e(
                        "BroccoliMarket",
                        "Illegal Status in Edit: Not found article ID"
                    )
                    return@registerForActivityResult
                }
                setContents(articleId)
            }
        articleTitleTextView = findViewById(R.id.article_title)
        articleAuthorTextView = findViewById(R.id.article_author)
        articlePriceTextView = findViewById(R.id.article_price)
        articleDateTextView = findViewById(R.id.article_date)
        articleContentTextView = findViewById(R.id.article_content)
        articleBackButton = findViewById(R.id.article_button_back)
        articleDeleteButton = findViewById(R.id.article_delete_button)
        articleResolveButton = findViewById(R.id.article_resolve_button)
        articleActionButton = findViewById(R.id.article_action_button)
    }

    private fun setContents(articleId: String) {
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
                articlePriceTextView.text = "${articleData.price}원"
                articleAuthorTextView.text = userData.nickname
                articleDateTextView.text = ArticleData.formatDate(
                    Date.from(Instant.now()),
                    articleData.uploadTime
                )
                articleContentTextView.text = articleData.content
                if (userData.email == FirebaseAuthDelegate.currentUser?.email) {
                    articleDeleteButton.visibility = View.VISIBLE
                    articleResolveButton.visibility = View.VISIBLE
                    if (articleData.isResolved) {
                        articleResolveButton.isEnabled = false
                    }
                } else {
                    articleDeleteButton.visibility = View.INVISIBLE
                    articleResolveButton.visibility = View.INVISIBLE
                    articleActionButton.setImageResource(com.firebase.ui.auth.R.drawable.fui_ic_mail_white_24dp)
                }
            }
        }
    }

    override fun onPositiveButtonClickListener(dialog: ChatSendDialog) {
        println("test")
        val content = dialog.content
        val sender = FirebaseAuthDelegate.currentUser?.email!!
        val receiver = articleData.authorEmail
        if (sender == receiver) {
            return
        }
        ChatDataRepositoryDelegate.repository
            .sendChat(
                sender,
                receiver,
                content,
                Date.from(Instant.now())
            )
            .addOnSuccessListener {
                val intent =
                    Intent(this@ArticleActivity, ChatActivity::class.java)
                startActivity(intent)
            }
    }

    override fun onNegativeButtonClickListener(dialog: ChatSendDialog) {
    }
}