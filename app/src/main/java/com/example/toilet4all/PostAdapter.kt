package com.example.toilet4all

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class PostAdapter(options: FirebaseRecyclerOptions<Post>): FirebaseRecyclerAdapter<Post, PostAdapter.ViewHolder>(options) {

    lateinit var itemClickListener: OnItemClickListener
    lateinit var itemLongClickListener: OnItemLongClickListener

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        var nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        var dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

        init {
            itemView.setOnClickListener {
                itemClickListener.onItemClick(it, adapterPosition)
            }

            itemView.setOnLongClickListener {
                itemLongClickListener.onItemLongClick(it, adapterPosition)
                true
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(view: View, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Post) {
        holder.titleTextView.text = model.title
        holder.dateTextView.text = "날짜 | " + model.date
        holder.nameTextView.text = "작성자 | " + model.name
    }

}