package service.broccolli.market

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.Query
import service.broccolli.market.adapter.ArticleListItemAdapter
import service.firebase.ArticleData
import service.firebase.ArticleDataRepositoryDelegate
import service.firebase.UserDataRepositoryDelegate
import service.firebase.auth.FirebaseAuthDelegate
import kotlin.math.min

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

        // auto-fetch on scroll
        recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                if (!recyclerView.canScrollVertically(1)) {
                    val list = recyclerAdapter.getItemList()
                    val lastItem = list.lastOrNull()
                    if (lastItem == null) {
                        prepareArticles()
                        return
                    }
                    val maxCount = 10
                    ArticleDataRepositoryDelegate.repository.collection
                        .orderBy("uploadTime", Query.Direction.DESCENDING)
                        .whereLessThan("uploadTime", lastItem.uploadTime)
                        .limit(maxCount.toLong())
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val articleDataList =
                                snapshot.map { ArticleData(it) }.toMutableList()
                            attachArticles(articleDataList, false)
                        }
                        .addOnFailureListener {
                            Log.e("BroccoliMarket", it.message.toString())
                        }
                }
            }
        })

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
                    "articlePublish", "articleDelete" -> prepareArticles()
                    "articleEdit" -> {
                        val articleId = intent.getStringExtra("articleId")!!
                        prepareArticles()
                        startArticle(articleId)
                    }
                }
            }
        recyclerView = findViewById(R.id.activity_main_article_view)
        recyclerAdapter =
            ArticleListItemAdapter(activityResultLauncher, mutableListOf())
        recyclerView.layoutManager = RecyclerViewWrapperLayoutManager(this)
        recyclerView.adapter = recyclerAdapter
        articlePublishButton =
            findViewById(R.id.activity_main_article_publish_button)
    }

    private fun startArticle(articleId: String) {
        val intent = Intent(this, ArticleActivity::class.java)
            .putExtra("articleId", articleId)
        activityResultLauncher.launch(intent)
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
        val maxCount = 30
        ArticleDataRepositoryDelegate.repository.collection
            .orderBy("uploadTime", Query.Direction.DESCENDING)
            .limit(maxCount.toLong())
            .get()
            .addOnSuccessListener { snapshot ->
                val articleDataList =
                    snapshot.map { ArticleData(it) }.toMutableList()
                attachArticles(articleDataList)
            }
            .addOnFailureListener {
                Log.e("BroccoliMarket", it.message.toString())
            }
    }

    private fun attachArticles(
        articleDataList: MutableList<ArticleData>,
        replace: Boolean = true
    ) {
        val maxCount = 30
        if (replace) {
            recyclerAdapter.clear()
        }
        val currentItemCount = recyclerAdapter.itemCount
        recyclerAdapter.fetchItems(articleDataList)
        val newItemCount = recyclerAdapter.itemCount
        val attachedItemCount = newItemCount - currentItemCount
        if (attachedItemCount == 0) {
            return
        }
        recyclerAdapter.notifyItemRangeInserted(
            currentItemCount,
            min(maxCount, newItemCount - currentItemCount)
        )
    }

    // https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in
    private class RecyclerViewWrapperLayoutManager(context: Context) :
        LinearLayoutManager(context) {
        override fun onLayoutChildren(
            recycler: RecyclerView.Recycler?,
            state: RecyclerView.State?
        ) {
            try {
                super.onLayoutChildren(recycler, state)
            } catch (e: IndexOutOfBoundsException) {
                Log.e("TAG", "meet a IOOBE in RecyclerView")
            }
        }
    }
}