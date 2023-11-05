package service.firebase.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FirebaseAuthDelegate private constructor() {
    companion object {
        private val auth: FirebaseAuth = Firebase.auth

        val currentUser: FirebaseUser?
            get() = auth.currentUser

        fun createNewUser(email: String, password: String): Task<AuthResult> {
            return auth.createUserWithEmailAndPassword(email, password)
        }

        fun signIn(email: String, password: String): Task<AuthResult> {
            return auth.signInWithEmailAndPassword(email, password)
        }

        fun signOut() {
            return auth.signOut()
        }
    }
}