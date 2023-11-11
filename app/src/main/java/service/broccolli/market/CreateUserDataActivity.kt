package service.broccolli.market

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import service.broccolli.util.EditTextUtil.Companion.addContentErrorMessageChecker
import service.firebase.UserDataRepositoryDelegate
import service.firebase.auth.FirebaseAuthDelegate

class CreateUserDataActivity : AppCompatActivity() {
    private lateinit var nicknameEditText: EditText
    private lateinit var birthdayEditText: EditText
    private lateinit var createUserButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user_data)
        initialize()
        nicknameEditText.addContentErrorMessageChecker(CreateUserDataActivity::getNicknameErrorMessage)
        createUserButton.setOnClickListener {
            val email = FirebaseAuthDelegate.currentUser!!.email!!
            val nickname = nicknameEditText.text.toString()
            val birthday = birthdayEditText.text.toString()
            UserDataRepositoryDelegate.repository
                .add(email, nickname, birthday)
                .addOnSuccessListener(this) {
                    setResult(
                        RESULT_OK,
                        Intent().putExtra("intent", "createUserData")
                    )
                    finish()
                }
        }
    }

    private fun initialize() {
        nicknameEditText =
            findViewById(R.id.create_user_data_nickname_edit_text)
        birthdayEditText =
            findViewById(R.id.create_user_data_birthday_edit_text)
        createUserButton =
            findViewById(R.id.create_user_data_create_user_button)
    }

    companion object {
        private fun getNicknameErrorMessage(nickname: String): String? {
            return null
        }
    }
}