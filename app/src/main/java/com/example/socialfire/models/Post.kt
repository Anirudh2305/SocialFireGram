package com.example.socialfire.models

import com.google.firebase.firestore.PropertyName

data class Post(
    var description:String = "",
    @get:PropertyName("image_url") @set:PropertyName("image_url") var imageUrl:String ="", // This get set is used only
    @get:PropertyName("creation_time_ms") @set:PropertyName("creation_time_ms") var creationTimeMs:Long = 0,// when firebase console name not same as variable name user here
    var user:User? = null
)
// Rule for Firestore data class. The params should be var and need to have default values.

