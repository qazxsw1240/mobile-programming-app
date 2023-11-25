package service.broccolli.market.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import service.broccolli.market.R
import service.firebase.UserDataRepositoryDelegate
import service.firebase.model.ArticleData
import service.firebase.model.UserData
import java.time.Instant

class ArticleListItem(view: View) : RecyclerView.ViewHolder(view) {
    private var titleView: TextView =
        view.findViewById(R.id.article_list_title)
    private var authorView: TextView =
        view.findViewById(R.id.article_list_author)
    private var timeView: TextView =
        view.findViewById(R.id.article_list_time)
    private var isResolvedView: TextView =
        view.findViewById(R.id.article_list_is_resolved)

    fun setContents(articleData: ArticleData) {
        UserDataRepositoryDelegate.repository
            .get(articleData.authorEmail)
            .addOnSuccessListener {
                val userData = UserData(it)
                titleView.text = articleData.title
                authorView.text = userData.nickname
                timeView.text = ArticleData.formatDate(
                    Instant.now(),
                    articleData.uploadTime.toInstant()
                )
                isResolvedView.text = if (articleData.isResolved) {
                    STRING_RESOLVED
                } else {
                    STRING_UNRESOLVED
                }
            }
    }

    companion object {
        const val STRING_RESOLVED = "판매 완료"
        const val STRING_UNRESOLVED = "판매 중"
    }
}