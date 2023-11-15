package service.broccolli.market.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import service.broccolli.market.R
import service.firebase.ArticleData
import java.time.Instant
import java.util.Date

class ArticleListItem(private val view: View) : RecyclerView.ViewHolder(view) {
    private lateinit var titleView: TextView
    private lateinit var authorView: TextView
    private lateinit var timeView: TextView
    private lateinit var isResolvedView: TextView
    fun setContents(articleData: ArticleData) {
        titleView = view.findViewById(R.id.article_list_title)
        authorView = view.findViewById(R.id.article_list_author)
        timeView = view.findViewById(R.id.article_list_time)
        isResolvedView = view.findViewById(R.id.article_list_is_resolved)
        titleView.text = articleData.title
        authorView.text = articleData.authorEmail
        timeView.text = ArticleData.formatDate(
            Date.from(Instant.now()),
            articleData.uploadTime
        )
        isResolvedView.text = if (articleData.isResolved) {
            "판매 완료"
        } else {
            "판매 중"
        }
    }
}