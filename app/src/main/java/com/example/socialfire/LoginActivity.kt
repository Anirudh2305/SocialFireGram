package com.example.socialfire

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.socialfire.models.User
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.tasks.await

private const val TAG = "LoginActivity"
const val REQUEST_CODE = 0

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseDb=FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        if(auth.currentUser!=null) {
            goPostsActivity()
        }

        btnLogin.setOnClickListener{
            btnLogin.isEnabled = false
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            if(email.isBlank() || password.isBlank()) {
                btnLogin.isEnabled = true
                Toast.makeText(this,"Email/Password can't be empty",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task->
                btnLogin.isEnabled = true
                if(task.isSuccessful) {
                    Toast.makeText(this,"Success!",Toast.LENGTH_SHORT).show()
                    goPostsActivity()
                } else {
                    Log.e(TAG, "signInWithEmail failed",task.exception)
                    Toast.makeText(this,"Authentication failed",Toast.LENGTH_SHORT).show()
                }
            }
        }

        GSignInButton.setOnClickListener {
            signInGoogleId()
        }
    }

    private fun goPostsActivity() {
        Log.i(TAG, "goPostsActivity")
        val intent  = Intent(this,PostsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun signInGoogleId() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.webClient_id))
            .requestEmail()
            .requestProfile()
            .build()

        val signInClient = GoogleSignIn.getClient(this,options)
        signInClient.signOut()

        signInClient.signInIntent.also {
            startActivityForResult(it, REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
            account?.let {
                googleAuthFromFirebase(it)
            }
        }
        else {
            Toast.makeText(this,"Account not selected",Toast.LENGTH_SHORT).show()
        }
    }

    private fun googleAuthFromFirebase(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credentials).addOnCompleteListener { task->
            if(task.isSuccessful) {
                val docId = auth.currentUser?.uid as String
                val userDocRef = firebaseDb.collection("users").document(docId)
                userDocRef.get().addOnCompleteListener { task2->
                    if(task2.isSuccessful) {
                        val document = task2.getResult()
                        if(!document.exists())
                        {
                            val user = User(auth.currentUser?.displayName as String,18)
                            firebaseDb.collection("users").document(docId).set(user)
                        }
                    }
                }

                goPostsActivity()
            }
            else
                Log.e(TAG, "signInWithGoogle failed",task.exception)
        }
    }
}