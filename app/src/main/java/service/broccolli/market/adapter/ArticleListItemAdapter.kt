package service.broccolli.market.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import service.broccolli.market.ArticleActivity
import service.broccolli.market.R
import service.firebase.ArticleData

class ArticleListItemAdapter(
    private val activityResultLauncher: ActivityResultLauncher<Intent>,
    private val articleLIstItems: MutableList<ArticleData>
) :
    RecyclerView.Adapter<ArticleListItem>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ArticleListItem {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view =
            layoutInflater.inflate(R.layout.article_list_item, parent, false)
        val viewHolder = ArticleListItem(view)
        view.setOnClickListener {
            val articleListItem = articleLIstItems[viewHolder.layoutPosition]
            val intent = Intent(parent.context, ArticleActivity::class.java)
                .putExtra("articleId", articleListItem.id)
            activityResultLauncher.launch(intent)
        }
        return viewHolder
    }

    override fun getItemCount(): Int {
        return articleLIstItems.size
    }

    override fun onBindViewHolder(holder: ArticleListItem, position: Int) {
        holder.setContents(articleLIstItems[position])
    }

    fun getItemList(): List<ArticleData> = articleLIstItems

    fun clear() = this.articleLIstItems.clear()

    fun fetchItems(newArticleLIstItems: MutableList<ArticleData>) {
        articleLIstItems.addAll(newArticleLIstItems)
    }
}