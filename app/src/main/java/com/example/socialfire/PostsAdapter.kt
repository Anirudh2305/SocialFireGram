package com.example.socialfire

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socialfire.models.Post
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.item_post.view.*
import java.math.BigInteger
import java.security.MessageDigest

class PostsAdapter(private val context: Context, private val posts: List<Post>, private val listener: OnPostClickListener) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {

        val likeButton = itemView.findViewById<ImageView>(R.id.btnLike)

        fun bind(post:Post) {
            val username = post.user?.username as String
            itemView.tvUsername.text = post.user?.username
            itemView.tvDescription.text = post.description
            Glide.with(context).load(post.imageUrl).into(itemView.ivPost)
            itemView.tvRelativeTime.text = DateUtils.getRelativeTimeSpanString(post.creationTimeMs)
            Glide.with(context).load(getProfileImageUrl(username)).circleCrop().into(itemView.ivProfileImage)

            val currentUserid = FirebaseAuth.getInstance().currentUser!!.uid
            val isLiked = post.likedBy.contains(currentUserid)

            if(isLiked) {
                itemView.btnLike.setImageResource(R.drawable.baseline_favorite_24)
            } else {
                itemView.btnLike.setImageResource(R.drawable.baseline_unliked)
            }

            itemView.tvLikeCount.text=post.likedBy.size.toString()
        }

        private fun getProfileImageUrl(username: String): String {
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(username.toByteArray())
            val bigInt = BigInteger(hash)
            val hex = bigInt.abs().toString(16)
            return "https://www.gravatar.com/avatar/$hex?d=identicon"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_post,parent,false))
        viewHolder.likeButton.setOnClickListener{
            listener.onPostClick(viewHolder.adapterPosition)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int {
        return posts.size
    }
}

interface OnPostClickListener {
    fun onPostClick(position : Int)
}