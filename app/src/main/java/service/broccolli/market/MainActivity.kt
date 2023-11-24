package service.broccolli.market

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.Query
import service.broccolli.market.adapter.ArticleListItemAdapter
import service.broccolli.market.fragment.ArticleFilterDialog
import service.firebase.ArticleDataRepositoryDelegate
import service.firebase.UserDataRepositoryDelegate
import service.firebase.auth.FirebaseAuthDelegate
import service.firebase.model.ArticleData
import kotlin.math.min

class MainActivity : AppCompatActivity(),
    ArticleFilterDialog.ArticleFilterListener {
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: ArticleListItemAdapter

    private lateinit var articleFilterButton: Button
    private lateinit var articlePublishButton: FloatingActionButton
    private lateinit var articleChatButton: FloatingActionButton

    private var filterOption: Int = ArticleFilterDialog.FILTER_ALL_ARTICLES
    private var minPrice: Int? = null
    private var maxPrice: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialize()

        if (FirebaseAuthDelegate.currentUser != null) {
            prepareArticles()
        } else {
            // start auth steps
            startSignInActivity()
            return
        }

        articleFilterButton.setOnClickListener {
            intent.putExtra("filterOption", filterOption)
            intent.putExtra("minPrice", minPrice)
            intent.putExtra("maxPrice", maxPrice)
            ArticleFilterDialog()
                .show(
                    supportFragmentManager,
                    "articleFilterDialog"
                )
        }

        articlePublishButton.setOnClickListener {
            startArticlePublishActivity()
        }

        articleChatButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        // auto-fetch on scroll
        recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                if (!recyclerView.canScrollVertically(1)) {
                    fetchArticles()
                }
            }
        })
    }

    override fun onPositiveButtonClickListener(dialog: ArticleFilterDialog) {
        filterOption = dialog.filterOption
        minPrice = dialog.minPrice
        maxPrice = dialog.maxPrice
        println("$filterOption, $minPrice, $maxPrice")
        prepareArticles()
    }

    override fun onNegativeButtonClickListener(dialog: ArticleFilterDialog) {

    }

    private fun initialize() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                if (activityResult.resultCode != RESULT_OK) {
                    return@registerForActivityResult
                }
                val intent = activityResult.data
                when (intent?.getStringExtra("intent")) {
                    "signIn" -> handleSignInActivity()
                    "createUserData" -> handleCreateUserDataActivity()
                    "articlePublish", "articleDelete" -> prepareArticles()
                    else -> prepareArticles()
                }
            }
        recyclerView = findViewById(R.id.activity_main_article_view)
        recyclerAdapter =
            ArticleListItemAdapter(activityResultLauncher, mutableListOf())
        recyclerView.layoutManager = RecyclerViewWrapperLayoutManager(this)
        recyclerView.adapter = recyclerAdapter
        articleFilterButton =
            findViewById(R.id.activity_main_article_filter_button)
        articlePublishButton =
            findViewById(R.id.activity_main_article_publish_button)
        articleChatButton = findViewById(R.id.activity_main_article_chat_button)
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

    private fun fetchArticles() {
        val list = recyclerAdapter.getItemList()
        val lastItem = list.lastOrNull()
        if (lastItem == null) {
            prepareArticles()
            return
        }
        val maxCount = 10
        var query = ArticleDataRepositoryDelegate.repository.collection
            .whereLessThan("uploadTime", lastItem.uploadTime)
        minPrice?.let {
            query = ArticleDataRepositoryDelegate.repository.collection
                .whereGreaterThanOrEqualTo("price", it)
                .orderBy("price", Query.Direction.ASCENDING)
                .orderBy("uploadTime", Query.Direction.DESCENDING)
        }
        maxPrice?.let {
            query = ArticleDataRepositoryDelegate.repository.collection
                .whereLessThanOrEqualTo("price", it)
                .orderBy("price", Query.Direction.ASCENDING)
                .orderBy("uploadTime", Query.Direction.DESCENDING)
        }
        when (filterOption) {
            ArticleFilterDialog.FILTER_RESOLVED_ARTICLES ->
                query = query.whereEqualTo("isResolved", true)

            ArticleFilterDialog.FILTER_UNRESOLVED_ARTICLES ->
                query = query.whereEqualTo("isResolved", false)

        }
        query
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

    private fun prepareArticles() {
        val maxCount = 30
        var query: Query =
            ArticleDataRepositoryDelegate.repository.collection
                .orderBy("uploadTime", Query.Direction.DESCENDING)
        minPrice?.let {
            query =
                ArticleDataRepositoryDelegate.repository.collection
                    .whereGreaterThanOrEqualTo("price", it)
                    .orderBy("price", Query.Direction.ASCENDING)
                    .orderBy("uploadTime", Query.Direction.DESCENDING)
        }
        maxPrice?.let {
            query = ArticleDataRepositoryDelegate.repository.collection
                .whereLessThanOrEqualTo("price", it)
                .orderBy("price", Query.Direction.ASCENDING)
                .orderBy("uploadTime", Query.Direction.DESCENDING)
        }
        when (filterOption) {
            ArticleFilterDialog.FILTER_RESOLVED_ARTICLES ->
                query =
                    query.whereEqualTo(
                        "isResolved",
                        true
                    )

            ArticleFilterDialog.FILTER_UNRESOLVED_ARTICLES ->
                query =
                    query.whereEqualTo(
                        "isResolved",
                        false
                    )
        }
        query
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