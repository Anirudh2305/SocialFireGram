package com.example.socialfire

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socialfire.models.Post
import com.example.socialfire.models.User
import kotlinx.android.synthetic.main.item_userpost.view.*

class UserPostAdapter(private val context: Context,private val posts: List<Post>) :
    RecyclerView.Adapter<UserPostAdapter.ViewHolder>() {

    private lateinit var mListener : OnItemClickListener
    interface OnItemClickListener {
        fun onItemClick(position : Int)
    }
    fun setOnItemClickListener(listener:OnItemClickListener) {
        mListener=listener
    }

    inner class ViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        fun bind(post: Post) {
            Glide.with(context).load(post.imageUrl).into(itemView.ivUserPost)
        }

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_userpost,parent,false)
        return ViewHolder(view,mListener)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }
}