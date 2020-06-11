package com.example.toilet4all

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_board.*

class BoardActivity : AppCompatActivity() {

    lateinit var layoutManager: LinearLayoutManager
    lateinit var rdb: DatabaseReference
    lateinit var adapter: PostAdapter
    var lastPid: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        init()
        getExtra()
        initBtn()
    }

    private fun getExtra() {
        if (intent.hasExtra("post")) {
            val post = intent.getSerializableExtra("post") as Post
            rdb.child(post.pid.toString()).setValue(post)
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

    private fun init() {
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        rdb = FirebaseDatabase.getInstance().getReference("boards/posts")

        /* init last pid */
        rdb.orderByChild("pid").limitToLast(1)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    for (child in p0.children) {
                        lastPid = child.key!!.toLong()
                    }
                    lastPid++
                }
            })

        /* set recycler view */
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
                val targetPid = adapter.getItem(position).pid
                val targetPassword = adapter.getItem(position).password
                val builder = AlertDialog.Builder(this@BoardActivity)
                var passwordEditText = EditText(this@BoardActivity)
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()

                builder.setTitle("게시글 삭제")
                    .setMessage("비밀번호를 입력하세요.")
                    .setCancelable(true)
                    .setView(passwordEditText)
                    .setPositiveButton("삭제") { _, _ ->
                        if (passwordEditText.text.toString() == targetPassword) {
                            removePost(targetPid)
                            Toast.makeText(
                                this@BoardActivity,
                                "해당 게시글이 삭제되었습니다.",
                                Toast.LENGTH_SHORT).show()
                        }
                        else
                            Toast.makeText(
                                this@BoardActivity,
                                "비밀번호가 일치하지 않습니다.",
                                Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("취소") { _, _ -> }
                builder.create().show()
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