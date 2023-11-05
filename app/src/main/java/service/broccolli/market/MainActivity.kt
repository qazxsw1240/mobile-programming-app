package service.broccolli.market

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import service.firebase.UserDataRepositoryDelegate
import service.firebase.auth.FirebaseAuthDelegate

class MainActivity : AppCompatActivity() {
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialize()
        if (FirebaseAuthDelegate.currentUser != null) {
            // start main application
        } else {
            // start auth steps
            startSignInActivity()
        }
    }

    private fun initialize() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                if (activityResult.resultCode != RESULT_OK) {
                    return@registerForActivityResult
                }
                val intent =
                    activityResult.data ?: return@registerForActivityResult
                when (intent.getStringExtra("intent")) {
                    "signIn" -> handleSignInActivity()
                    "createUserData" -> handleCreateUserDataActivity()
                }
            }
    }

    private fun startSignInActivity() {
        val intent = Intent(this, SignInActivity::class.java)
        activityResultLauncher.launch(intent)
    }

    private fun handleSignInActivity() {
        val authEmail = FirebaseAuthDelegate.currentUser!!.email!!
        UserDataRepositoryDelegate.repository.get(authEmail)
            .addOnSuccessListener(this) { result ->
                if (!result.exists()) {
                    startCreateUserDataActivity()
                    return@addOnSuccessListener
                }
            }
            .addOnFailureListener {
                Log.w("BroccoliMarket", "user not found")
            }
    }

    private fun startCreateUserDataActivity() {
        val intent = Intent(this, CreateUserDataActivity::class.java)
        activityResultLauncher.launch(intent)
    }

    private fun handleCreateUserDataActivity() {
        val authEmail = FirebaseAuthDelegate.currentUser!!.email!!
        UserDataRepositoryDelegate.repository.get(authEmail)
            .addOnSuccessListener(this) { result ->
                if (!result.exists()) {
                    Log.w("BroccoliMarket", "user not found after registration")
                    return@addOnSuccessListener
                }
            }
            .addOnFailureListener {
                Log.w("BroccoliMarket", "user not found")
            }
    }
}