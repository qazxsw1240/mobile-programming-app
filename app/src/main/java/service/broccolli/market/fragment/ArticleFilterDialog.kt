package service.broccolli.market.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import service.broccolli.market.R

class ArticleFilterDialog : DialogFragment() {
    private lateinit var articleFilterOption0: RadioButton
    private lateinit var articleFilterOption1: RadioButton
    private lateinit var articleFilterOption2: RadioButton
    private lateinit var articleFilterMinPrice: EditText
    private lateinit var articleFilterMaxPrice: EditText

    private var listener: ArticleFilterListener? = null

    var filterOption = FILTER_ALL_ARTICLES
        private set
    var minPrice: Int? = null
        private set
    var maxPrice: Int? = null
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(
        R.layout.dialog_article_filter,
        container,
        false
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as ArticleFilterListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = it.layoutInflater
            val view = inflater.inflate(R.layout.dialog_article_filter, null)
            initialize(view)
            attachEventListeners()
            builder
                .setView(view)
                .setPositiveButton("확인") { _, _ ->
                    listener?.onPositiveButtonClickListener(this)
                }
                .setNegativeButton("취소") { _, _ ->
                    listener?.onNegativeButtonClickListener(this)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setFilterOption(option: Int) {
        filterOption = option
    }

    private fun initialize(view: View) {
        articleFilterOption0 =
            view.findViewById(R.id.article_filter_option0)
        articleFilterOption1 =
            view.findViewById(R.id.article_filter_option1)
        articleFilterOption2 =
            view.findViewById(R.id.article_filter_option2)
        articleFilterMinPrice = view.findViewById(R.id.article_filter_min_price)
        articleFilterMaxPrice = view.findViewById(R.id.article_filter_max_price)
    }

    private fun attachEventListeners() {
        articleFilterOption0.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                println("selected $FILTER_ALL_ARTICLES")
                setFilterOption(FILTER_ALL_ARTICLES)
            }
        }
        articleFilterOption1.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                println("selected $FILTER_UNRESOLVED_ARTICLES")
                setFilterOption(FILTER_UNRESOLVED_ARTICLES)
            }
        }
        articleFilterOption2.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                println("selected $FILTER_RESOLVED_ARTICLES")
                setFilterOption(FILTER_RESOLVED_ARTICLES)
            }
        }
        articleFilterMinPrice.addTextChangedListener {
            val content = it.toString()
            minPrice = content.toIntOrNull()
        }
        articleFilterMaxPrice.addTextChangedListener {
            val content = it.toString()
            maxPrice = content.toIntOrNull()
        }
    }

    interface ArticleFilterListener {
        fun onPositiveButtonClickListener(dialog: ArticleFilterDialog)
        fun onNegativeButtonClickListener(dialog: ArticleFilterDialog)
    }

    companion object {
        const val FILTER_ALL_ARTICLES = 0
        const val FILTER_UNRESOLVED_ARTICLES = 1
        const val FILTER_RESOLVED_ARTICLES = 2
    }
}
