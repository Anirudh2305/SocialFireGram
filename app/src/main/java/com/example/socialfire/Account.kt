package com.example.socialfire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.socialfire.models.Post
import com.example.socialfire.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_account.*
import java.math.BigInteger
import java.security.MessageDigest

private const val TAG = "AccountActivity"

class Account : AppCompatActivity() {

    private var signedInUser: User?=null
    private lateinit var firebaseDb: FirebaseFirestore
    private lateinit var posts: MutableList<Post>
    //private lateinit var adapter: PostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        firebaseDb = FirebaseFirestore.getInstance()
        posts = mutableListOf()

        //rv Setup kro
        val adapter = UserPostAdapter(this,posts)
        rvUserPost.adapter = adapter
        rvUserPost.layoutManager = GridLayoutManager(this,3)
        adapter.setOnItemClickListener(object : UserPostAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                //Toast.makeText(this@Account,"Item clicked is: $position",Toast.LENGTH_SHORT).show()
                val intent = Intent(this@Account,ProfileActivity::class.java)
                intent.putExtra(EXTRA_USERNAME,signedInUser?.username)
                startActivity(intent)
            }
        })


        firebaseDb.collection("users").document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get().addOnSuccessListener {userSnapshot->
                signedInUser = userSnapshot.toObject(User::class.java)
                val currUsername = signedInUser?.username
                supportActionBar?.title = currUsername

                val postsReference = firebaseDb.collection("posts").
                limit(20).orderBy("creation_time_ms", Query.Direction.DESCENDING)
                    .whereEqualTo("user.username",currUsername)

                postsReference.addSnapshotListener {snapshot, exception->
                    if(exception!=null || snapshot==null) {
                        Log.e(TAG,"Exception when querying",exception)
                        return@addSnapshotListener
                    }
                    val postList = snapshot.toObjects(Post::class.java)
                    posts.clear()
                    posts.addAll(postList)
                    adapter.notifyDataSetChanged()
                }

                // Loading Account info
                Glide.with(this).load(getProfileImageUrl(signedInUser!!.username)).circleCrop().into(DisplayPicture)
                DisplayName.text = signedInUser!!.username
                DisplayAge.text = signedInUser!!.age.toString()
            }
            .addOnFailureListener{
                Log.e(TAG,"Failure fetching current user",it)
                Toast.makeText(this,"User not fetched",Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_logout) {
            Log.i(TAG,"Logout")
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this,RegisterActivity::class.java)
            // Once logged out and gone to login screen we clear entire backstack with this addFlags. So back press
            // on LoginAct will close app rather than going to profile screen again.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getProfileImageUrl(username: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hash = digest.digest(username.toByteArray())
        val bigInt = BigInteger(hash)
        val hex = bigInt.abs().toString(16)
        return "https://www.gravatar.com/avatar/$hex?d=identicon"
    }
}