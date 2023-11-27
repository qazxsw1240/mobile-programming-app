package service.broccolli.market

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import service.firebase.auth.FirebaseAuthDelegate

class SignInActivity : AppCompatActivity() {
    private lateinit var signUpActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var signInButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        initialize()

        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            signUpActivityLauncher.launch(intent)
        }

        signInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            FirebaseAuthDelegate.signIn(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        setResult(
                            RESULT_OK,
                            Intent().putExtra("intent", "signIn")
                        )
                        finish()
                        return@addOnCompleteListener
                    }
                    Toast
                        .makeText(
                            this@SignInActivity,
                            "로그인하지 못했습니다.",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
        }
    }

    private fun initialize() {
        signUpActivityLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
                this::onSignUpSuccess
            )
        emailEditText = findViewById(R.id.sign_in_email_address_edit_text)
        passwordEditText = findViewById(R.id.sign_in_password_edit_text)
        signUpButton = findViewById(R.id.signup_button)
        signInButton = findViewById(R.id.signin_button)
    }

    private fun onSignUpSuccess(activityResult: ActivityResult) {
        val intent = activityResult.data ?: return
        val email = intent.getStringExtra("email") ?: return
        emailEditText.setText(email, TextView.BufferType.EDITABLE)
    }
}