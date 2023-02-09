package com.example.socialfire

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.socialfire.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.etEmail
import kotlinx.android.synthetic.main.activity_login.etPassword
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "RegisterActivity"


class RegisterActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    private lateinit var firebaseDb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        txtAlreadyReg.text = getString(R.string.registerAsk2)
        txtAlreadyReg.paintFlags=Paint.UNDERLINE_TEXT_FLAG

        firebaseDb = FirebaseFirestore.getInstance()

        auth=FirebaseAuth.getInstance()

        if(auth.currentUser!=null) {
            startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
            finish()
        }
        btnRegister.setOnClickListener {
            btnRegister.isEnabled=false
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val name = etName.text.toString()
            val age = etAge.text.toString()
            if(email.isBlank() || password.isBlank() || name.isBlank() || age.isBlank()) {
                btnRegister.isEnabled = true
                Toast.makeText(this,"Email/Password can't be empty",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            registerUser(email,password,name,age)
        }

        txtAlreadyReg.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }

    private fun registerUser(email:String,password:String,name:String,age:String) {
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task->
            btnRegister.isEnabled=true
            if(task.isSuccessful) {
                // dB mein add kro user
                val user = User(name,age.toInt())
                val id = auth.currentUser?.uid as String
                firebaseDb.collection("users").document(id).set(user)
                startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
                finish()
            }
            else {
                    Log.e(TAG, "createWithEmail failed",task.exception)
                    Toast.makeText(this@RegisterActivity,"Unable to Register",Toast.LENGTH_SHORT).show()
            }
        }
    }

}