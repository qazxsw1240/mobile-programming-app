package service.broccolli.market

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.Query
import service.broccolli.market.adapter.ArticleListItem
import service.broccolli.market.adapter.ArticleListItemAdapter
import service.firebase.ArticleData
import service.firebase.ArticleDataRepositoryDelegate
import service.firebase.UserDataRepositoryDelegate
import service.firebase.auth.FirebaseAuthDelegate

class MainActivity : AppCompatActivity() {
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: ArticleListItemAdapter

    private lateinit var articlePublishButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialize()

        articlePublishButton.setOnClickListener {
            startArticlePublishActivity()
        }

        if (FirebaseAuthDelegate.currentUser != null) {
            prepareArticles()
        } else {
            // start auth steps
            startSignInActivity()
        }
    }

    private fun initialize() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                if (activityResult.resultCode != RESULT_OK) {
                    return@registerForActivityResult
                }
                val intent =
                    activityResult.data ?: return@registerForActivityResult
                when (intent.getStringExtra("intent")) {
                    "signIn" -> handleSignInActivity()
                    "createUserData" -> handleCreateUserDataActivity()
                    "articlePublish" -> prepareArticles()
                }
            }
        recyclerView = findViewById(R.id.activity_main_article_view)
        recyclerAdapter =
            ArticleListItemAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recyclerAdapter
        articlePublishButton =
            findViewById(R.id.activity_main_article_publish_button)
    }

    private fun startSignInActivity() {
        val intent = Intent(this, SignInActivity::class.java)
        activityResultLauncher.launch(intent)
    }

    private fun handleSignInActivity() {
        val authEmail = FirebaseAuthDelegate.currentUser!!.email!!
        UserDataRepositoryDelegate.repository.get(authEmail)
            .addOnSuccessListener(this) { result ->
                if (!result.exists()) {
                    startCreateUserDataActivity()
                    return@addOnSuccessListener
                }
            }
            .addOnFailureListener {
                Log.w("BroccoliMarket", "user not found")
            }
    }

    private fun startCreateUserDataActivity() {
        val intent = Intent(this, CreateUserDataActivity::class.java)
        activityResultLauncher.launch(intent)
    }

    private fun handleCreateUserDataActivity() {
        val authEmail = FirebaseAuthDelegate.currentUser!!.email!!
        UserDataRepositoryDelegate.repository.get(authEmail)
            .addOnSuccessListener(this) { result ->
                if (!result.exists()) {
                    Log.w("BroccoliMarket", "user not found after registration")
                    return@addOnSuccessListener
                }
            }
            .addOnFailureListener {
                Log.w("BroccoliMarket", "user not found")
            }
    }

    private fun startArticlePublishActivity() {
        val intent = Intent(this, ArticlePublishActivity::class.java)
        activityResultLauncher.launch(intent)
    }

    private fun prepareArticles() {
        ArticleDataRepositoryDelegate.repository.collection
            .orderBy("uploadTime", Query.Direction.DESCENDING)
            .limit(30)
            .get()
            .addOnSuccessListener {snapshot ->
                val articleDataList =
                    snapshot.map { ArticleData(it) }.toMutableList()
                recyclerAdapter.clear()
                recyclerAdapter.fetchItems(articleDataList)
                recyclerAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.e("BroccoliMarket", it.message.toString())
            }
    }
}