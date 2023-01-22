package com.example.socialfire

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.socialfire.models.User
import com.example.socialfire.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create.*

private const val TAG = "CreateActivity"
private const val PICK_PHOTO_CODE = 10
class CreateActivity : AppCompatActivity() {

    private var photoUri: Uri? = null
    private var signedInUser: User?=null
    private lateinit var storageReference: StorageReference
    private lateinit var firebaseDb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        storageReference = FirebaseStorage.getInstance().reference
        firebaseDb = FirebaseFirestore.getInstance()

        firebaseDb.collection("users").document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get().addOnSuccessListener {userSnapshot->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG,"signed in user: $signedInUser")
            }
            .addOnFailureListener {
                Log.e(TAG,"Failure fetching signed in user",it)
            }

        btnPickImage.setOnClickListener {
            Log.i(TAG,"Open image app")
            val imagePickerIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            //imagePickerIntent.type = "image/*"
            //startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            startActivityForResult(Intent.createChooser(imagePickerIntent,"Choose a Photo"), PICK_PHOTO_CODE) // Shows dialog only when multiple apps eligible
        }

        btnSubmit.setOnClickListener {
           handleSubmitClick()
        }
    }

    private fun handleSubmitClick() {
        if(photoUri ==  null) {
            Toast.makeText(this,"Image not picked",Toast.LENGTH_SHORT).show()
            return
        }

        if(etDescription.text.isBlank()) {
            Toast.makeText(this,"Description can't be empty",Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled=false
        val photoReference = storageReference.child("image/${System.currentTimeMillis()}-photo.jpg")
        photoReference.putFile(photoUri!!).
        continueWithTask {photoUploadTask->
            Log.i(TAG,"uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
            photoReference.downloadUrl
        }.continueWithTask { downloadUrlTask->
            val post = Post(
                etDescription.text.toString(),
                downloadUrlTask.result.toString(),
                System.currentTimeMillis(),
                signedInUser
            )
            firebaseDb.collection("posts").add(post)
        }.addOnCompleteListener { photoCreationTask->
            btnSubmit.isEnabled=true
            if(!photoCreationTask.isSuccessful) {
                Log.e(TAG,"Error in uploading/fetching image",photoCreationTask.exception)
                Toast.makeText(this,"Failed to save post",Toast.LENGTH_LONG).show()
            }
            etDescription.text.clear()
            imageView.setImageResource(0)
            Toast.makeText(this,"Success!",Toast.LENGTH_SHORT).show()
            val profileIntent = Intent(this, ProfileActivity::class.java)
            profileIntent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(profileIntent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_PHOTO_CODE) {  // To verify whether this has been called due to button click by user
            if(resultCode == Activity.RESULT_OK) {
                photoUri = data?.data
                Log.i(TAG,"Photo: $photoUri")
                imageView.setImageURI(photoUri)
            } else {
                Toast.makeText(this,"Image not picked!!",Toast.LENGTH_SHORT).show()
            }
        }
    }
}