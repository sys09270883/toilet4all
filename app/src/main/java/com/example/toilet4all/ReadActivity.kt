package com.example.toilet4all

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_read.*

class ReadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)
        getExtra()
        init()
    }

    private fun init() {
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getExtra() {
        if (intent.hasExtra("post")) {
            val post = intent.getSerializableExtra("post") as Post
            readTitleView.text = post.title
            nameTextView.text = "작성자 | " + post.name
            dateTextView.text = "날짜 | " + post.date
            contentTextView.text = post.content
        }
    }
}