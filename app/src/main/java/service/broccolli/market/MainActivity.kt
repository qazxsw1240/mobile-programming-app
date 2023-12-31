package service.broccolli.market

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import service.broccolli.market.adapter.ArticleListItemAdapter
import service.broccolli.market.fragment.ArticleFilterDialog
import service.firebase.ArticleDataRepositoryDelegate
import service.firebase.UserDataRepositoryDelegate
import service.firebase.auth.FirebaseAuthDelegate
import service.firebase.model.ArticleData
import kotlin.math.min

class MainActivity :
    AppCompatActivity(),
    ArticleFilterDialog.ArticleFilterListener {
    private val articleFilterOption = ArticleFilterOption(
        ArticleFilterDialog.FILTER_ALL_ARTICLES,
        null,
        null
    )

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: ArticleListItemAdapter

    private lateinit var signOutButton: Button
    private lateinit var articleFilterButton: Button
    private lateinit var articlePublishButton: FloatingActionButton
    private lateinit var articleChatButton: FloatingActionButton
    private lateinit var articleRefreshButton: FloatingActionButton

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

        signOutButton.setOnClickListener {
            FirebaseAuthDelegate.signOut()
            recreate()
        }

        articleFilterButton.setOnClickListener {
            intent.putExtra("filterOption", articleFilterOption.filterOption)
            intent.putExtra("minPrice", articleFilterOption.minPrice)
            intent.putExtra("maxPrice", articleFilterOption.maxPrice)
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

        articleRefreshButton.setOnClickListener {
            prepareArticles()
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
        articleFilterOption.filterOption = dialog.filterOption
        articleFilterOption.minPrice = dialog.minPrice
        articleFilterOption.maxPrice = dialog.maxPrice
        prepareArticles()
    }

    override fun onNegativeButtonClickListener(dialog: ArticleFilterDialog) {
    }

    private fun handleActivityForResult(activityResult: ActivityResult) {
        if (activityResult.resultCode != RESULT_OK) {
            return
        }
        val intent = activityResult.data ?: return
        when (intent.getStringExtra("intent")) {
            "signIn" -> handleSignInActivity()
            "createUserData" -> handleCreateUserDataActivity()
            "articlePublish", "articleDelete" -> prepareArticles()
            else -> prepareArticles()
        }
    }

    private fun initialize() {
        activityResultLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
                this::handleActivityForResult
            )
        recyclerView =
            findViewById(R.id.activity_main_article_view)
        recyclerAdapter =
            ArticleListItemAdapter(activityResultLauncher, mutableListOf())
        recyclerView.layoutManager =
            RecyclerViewWrapperLayoutManager(this)
        recyclerView.adapter =
            recyclerAdapter
        signOutButton =
            findViewById(R.id.activity_main_sign_out_button)
        articleFilterButton =
            findViewById(R.id.activity_main_article_filter_button)
        articlePublishButton =
            findViewById(R.id.activity_main_article_publish_button)
        articleChatButton =
            findViewById(R.id.activity_main_article_chat_button)
        articleRefreshButton =
            findViewById(R.id.activity_main_refresh_button)
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
                recreate()
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
                recreate()
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
        val filters = mutableListOf<Filter>()
        articleFilterOption.minPrice?.let {
            filters.add(Filter.greaterThanOrEqualTo("price", it))
        }
        articleFilterOption.maxPrice?.let {
            filters.add(Filter.lessThanOrEqualTo("price", it))
        }
        if (filters.isNotEmpty()) {
            query = ArticleDataRepositoryDelegate.repository.collection
                .where(Filter.and(*filters.toTypedArray()))
                .orderBy("price", Query.Direction.ASCENDING)
                .orderBy("uploadTime", Query.Direction.DESCENDING)
                .startAfter(lastItem.uploadTime)
        }
        when (articleFilterOption.filterOption) {
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
                articleDataList.sortByDescending { it.uploadTime }
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
        val filters = mutableListOf<Filter>()
        articleFilterOption.minPrice?.let {
            filters.add(Filter.greaterThanOrEqualTo("price", it))
        }
        articleFilterOption.maxPrice?.let {
            filters.add(Filter.lessThanOrEqualTo("price", it))
        }
        if (filters.isNotEmpty()) {
            query = ArticleDataRepositoryDelegate.repository.collection
                .where(Filter.and(*filters.toTypedArray()))
                .orderBy("price", Query.Direction.ASCENDING)
                .orderBy("uploadTime", Query.Direction.DESCENDING)
        }
        when (articleFilterOption.filterOption) {
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
                articleDataList.sortByDescending { it.uploadTime }
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

    private data class ArticleFilterOption(
        var filterOption: Int,
        var minPrice: Int?,
        var maxPrice: Int?
    )
}