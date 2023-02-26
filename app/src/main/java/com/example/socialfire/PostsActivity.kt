package com.example.socialfire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socialfire.models.Post
import com.example.socialfire.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_posts.*
import java.util.zip.Inflater

private const val TAG = "PostsActivity"
const val EXTRA_USERNAME = "extra"
open class PostsActivity : AppCompatActivity(),OnPostClickListener {

    private var signedInUser: User?=null
    private lateinit var firebaseDb:FirebaseFirestore
    private lateinit var posts: MutableList<Post>
    private lateinit var adapter: PostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        posts = mutableListOf()

        adapter = PostsAdapter(this,posts,this)
        rvPosts.adapter = adapter
        rvPosts.layoutManager=LinearLayoutManager(this)

        firebaseDb = FirebaseFirestore.getInstance()
        // firebaseDb = Firebase.firestore --- Both the commands do exact same thing

        firebaseDb.collection("users").document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get().addOnSuccessListener {userSnapshot->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG,"signed in user: $signedInUser")
            }
            .addOnFailureListener {
                Log.e(TAG,"Failure fetching signed in user",it)
            }

        var postsReference = firebaseDb.collection("posts").
        limit(20).orderBy("creation_time_ms",Query.Direction.DESCENDING)

        val currUsername = intent.getStringExtra(EXTRA_USERNAME)
        if(currUsername!=null) {
            supportActionBar?.title = currUsername
            postsReference = postsReference.whereEqualTo("user.username",currUsername)
        }

        postsReference.addSnapshotListener {snapshot, exception->
            if(exception!=null || snapshot==null) {
                Log.e(TAG,"Exception when querying",exception)
                return@addSnapshotListener
            }
            val postList = snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()
            for(post in postList) {
                Log.i(TAG,"Post: $post")
            }

            /*
            val map = HashMap<String,Any>()
            map["likedBy"] = ArrayList<String>()

            for(document in snapshot.documents) {
                //Log.i(TAG,"Document ${document.id}:${document.data}")
                val docRef = firebaseDb.collection("posts").
                document(document.id)

                docRef.update(map).addOnCompleteListener {
                    if(it.isSuccessful) {
                        Toast.makeText(this,"Kaam hogya",Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG,"game over")
                    }
                }
            }
            */
        }

        fabCreate.setOnClickListener {
            startActivity(Intent(this,CreateActivity::class.java))

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        /*
        if(item.itemId == R.id.menu_person) {
            val intent = Intent(this,ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME,signedInUser?.username)
            startActivity(intent)
        }
         */
        if(item.itemId == R.id.menu_person) {
            startActivity(Intent(this,Account::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPostClick(position: Int) {
        val post = posts[position]
        val currentUserid = FirebaseAuth.getInstance().currentUser!!.uid
        val isLiked = post.likedBy.contains(currentUserid)

        if(isLiked) {
            post.likedBy.remove(currentUserid)
        } else {
            post.likedBy.add(currentUserid)
        }
        // save this post to db
        // dbcollec.document(postId).update/set(post)
        var pos = position
        var postsReference = firebaseDb.collection("posts").
        limit(20).orderBy("creation_time_ms",Query.Direction.DESCENDING)

        postsReference.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "Exception when liking", exception)
                return@addSnapshotListener
            }
            val documents = snapshot.documents
            documents.forEach{
                if(pos==0)
                {
                    firebaseDb.collection("posts").document(it.id).set(post)
                }
                pos--
            }

        }
    }
}