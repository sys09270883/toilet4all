package com.example.toilet4all

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BoardAdapter(val items: MutableList<Board>): RecyclerView.Adapter<BoardAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var titleTextView = itemView.findViewById<TextView>(R.id.titleTextView)
        var contentTextView = itemView.findViewById<TextView>(R.id.contentTextView)
        var nameTextView = itemView.findViewById<TextView>(R.id.nameTextView)
        var dateTextView = itemView.findViewById<TextView>(R.id.dateTextView)

        init {
            itemView.setOnClickListener {
                itemClickListener.onItemClick(this, it, items[adapterPosition], adapterPosition)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(holder: ViewHolder, view: View, data: Board, position: Int)
    }

    lateinit var itemClickListener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleTextView.text = items[position].title
        holder.contentTextView.text = items[position].content
        holder.nameTextView.text = "작성자 | " + items[position].name
        holder.dateTextView.text = "날짜 | " + items[position].date
    }
}