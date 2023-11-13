package service.broccolli.market.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import service.broccolli.market.R
import service.firebase.ArticleData

class ArticleListItemAdapter(private val articleLIstItems: MutableList<ArticleData>) :
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
            Toast.makeText(
                parent.context,
                articleLIstItems[viewHolder.layoutPosition].title,
                Toast.LENGTH_SHORT
            ).show()
        }
        return viewHolder
    }

    override fun getItemCount(): Int {
        return articleLIstItems.size
    }

    override fun onBindViewHolder(holder: ArticleListItem, position: Int) {
        holder.setContents(articleLIstItems[position])
    }

    fun clear() {
        articleLIstItems.clear()
    }

    fun fetchItems(newArticleLIstItems: MutableList<ArticleData>) {
        articleLIstItems.addAll(newArticleLIstItems)
    }
}