package com.example.toilet4all

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_board.*
import kotlin.math.max

class BoardActivity : AppCompatActivity() {

    lateinit var layoutManager: LinearLayoutManager
    lateinit var rdb: DatabaseReference
    lateinit var adapter: PostAdapter
    var postCnt: Long = 0
    var lastPid: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        initCount()
        init()
        getExtra()
        initBtn()
    }

    private fun getExtra() {
        if (intent.hasExtra("post")) {
            val post = intent.getSerializableExtra("post") as Post
            rdb.child(post.pidx.toString()).setValue(post)
        }
    }

    private fun initBtn() {
        homeButton.setOnClickListener {
            onBackPressed()
        }

        writeButton.setOnClickListener {
            val intent = Intent(this, WriteActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("lastpidx", lastPid)
            startActivity(intent)
        }
    }

    private fun initCount() {
        val dbRef = FirebaseDatabase.getInstance().getReference("boards")
        dbRef.child("posts").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                postCnt = p0.childrenCount
                lastPid = max(postCnt, lastPid + 1)
                Log.d("postcnt", postCnt.toString())
            }
        })
    }

    private fun init() {
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        rdb = FirebaseDatabase.getInstance().getReference("boards/posts")
        val query = FirebaseDatabase.getInstance().reference.child("boards/posts")
            .orderByChild("pid").limitToLast(50)
        val option = FirebaseRecyclerOptions.Builder<Post>()
            .setQuery(query, Post::class.java).build()
        adapter = PostAdapter(option)
        adapter.itemClickListener = object : PostAdapter.OnItemClickListener {
            override fun onItemClick(
                view: View,
                position: Int
            ) {
                readPost(adapter.getItem(position))
            }
        }
        adapter.itemLongClickListener = object: PostAdapter.OnItemLongClickListener {
            override fun onItemLongClick(view: View, position: Int) {
                removePost(adapter.getItem(position).pidx)
            }
        }
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, layoutManager.orientation))
    }

    private fun removePost(pid: Long) {
        rdb.child(pid.toString()).removeValue()
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    private fun readPost(post: Post) {
        val intent = Intent(this, ReadActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("post", post)
        startActivity(intent)
    }

}