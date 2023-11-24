package service.broccolli.market.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import service.broccolli.market.R

class ChatSendDialog : DialogFragment() {
    private lateinit var contentView: EditText

    private var listener: ChatDialogListener? = null

    val content: String
        get() = contentView.text.toString()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(
        R.layout.dialog_chat,
        container,
        false
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as ChatDialogListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = it.layoutInflater
            val view = inflater.inflate(R.layout.dialog_chat, null)
            initialize(view)
            builder
                .setView(view)
                .setPositiveButton("보내기") { _, _ ->
                    listener?.onPositiveButtonClickListener(this)
                }
                .setNegativeButton("취소") { _, _ ->
                    listener?.onNegativeButtonClickListener(this)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun initialize(view: View) {
        contentView = view.findViewById(R.id.dialog_chat_content)
    }

    interface ChatDialogListener {
        fun onPositiveButtonClickListener(dialog: ChatSendDialog)
        fun onNegativeButtonClickListener(dialog: ChatSendDialog)
    }
}