package service.broccolli.util

import android.widget.EditText
import androidx.core.widget.addTextChangedListener

class EditTextUtil {
    companion object {
        fun EditText.addContentErrorMessageChecker(checker: (content: String) -> String?) {
            addTextChangedListener {
                val content = it.toString();
                val errorMessage = checker(content);
                if (errorMessage != null) {
                    error = errorMessage;
                }
            }
        }
    }
}