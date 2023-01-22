package com.example.socialfire

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socialfire.models.Post
import kotlinx.android.synthetic.main.item_post.view.*
import java.math.BigInteger
import java.security.MessageDigest

class PostsAdapter(private val context: Context, private val posts: List<Post>) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        fun bind(post:Post) {
            val username = post.user?.username as String
            itemView.tvUsername.text = post.user?.username
            itemView.tvDescription.text = post.description
            Glide.with(context).load(post.imageUrl).into(itemView.ivPost)
            itemView.tvRelativeTime.text = DateUtils.getRelativeTimeSpanString(post.creationTimeMs)
            Glide.with(context).load(getProfileImageUrl(username)).circleCrop().into(itemView.ivProfileImage)

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
        val view = LayoutInflater.from(context).inflate(R.layout.item_post,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int {
        return posts.size
    }
}