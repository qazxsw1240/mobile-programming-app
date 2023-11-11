package service.broccolli.market

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import service.broccolli.util.EditTextUtil.Companion.addContentErrorMessageChecker
import service.firebase.auth.FirebaseAuthDelegate

class SignUpActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordCheckEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var signUpCancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        initialize()
        emailEditText.addContentErrorMessageChecker(SignUpActivity::getEmailAddressErrorMessage)
        passwordEditText.addContentErrorMessageChecker(SignUpActivity::getPasswordErrorMessage)
        passwordCheckEditText.addContentErrorMessageChecker { content ->
            val password = passwordEditText.text.toString()
            if (password != content) {
                return@addContentErrorMessageChecker "비밀번호가 일치하지 않습니다."
            }
            return@addContentErrorMessageChecker null
        }
        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            FirebaseAuthDelegate.createNewUser(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        setResult(RESULT_OK, Intent().putExtra("email", email))
                        finish()
                    } else {
                        val exception =
                            task.exception ?: return@addOnCompleteListener
                        if (exception is FirebaseAuthUserCollisionException) {
                            emailEditText.error = "이메일이 이미 존재합니다."
                        }
                    }
                }
        }
        signUpCancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun initialize() {
        emailEditText = findViewById(R.id.sign_up_email_address_edit_text)
        passwordEditText = findViewById(R.id.sign_up_password_edit_text)
        passwordCheckEditText =
            findViewById(R.id.sign_up_password_check_edit_text)
        signUpButton = findViewById(R.id.sign_up_signup_button)
        signUpCancelButton = findViewById(R.id.sign_up_cancel_button)
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$")

        private fun getEmailAddressErrorMessage(email: String): String? {
            return when {
                !EMAIL_REGEX.matches(email) -> "이메일 형식이 올바르지 않습니다."
                else -> null
            }
        }

        private fun getPasswordErrorMessage(password: String): String? {
            return when {
                password.length < MIN_PASSWORD_LENGTH -> "비밀번호는 최소 ${MIN_PASSWORD_LENGTH}자여야 합니다."
                else -> null
            }
        }
    }
}