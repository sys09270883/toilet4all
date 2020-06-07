package com.example.toilet4all

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_write.*
import java.util.*

class WriteActivity : AppCompatActivity() {

    var pidx: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)
        getExtra()
        initBtn()
    }

    private fun initBtn() {
        backButton.setOnClickListener {
            onBackPressed()
        }

        cancelButton.setOnClickListener {
            onBackPressed()
        }

        saveButton.setOnClickListener {
            val post = Post(
                pidx,
                titleEditTextView.text.toString(),
                contentEditTextView.text.toString(),
                getDate(),
                nameEditTextView.text.toString(),
                passwordEditTextView.text.toString()
            )
            val intent = Intent(this, BoardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("post", post)
            startActivity(intent)
        }
    }

    private fun getDate(): String {
        var ret = ""
        val c = Calendar.getInstance()
        ret += c.get(Calendar.YEAR).toString() + " "
        ret += (c.get(Calendar.MONTH) + 1).toString() + " "
        ret += (c.get(Calendar.DATE) + 1).toString()
        return ret
    }

    private fun getExtra() {
        if (intent.hasExtra("lastpidx")) {
            pidx = intent.getLongExtra("lastpidx", -1)
            Log.d("pidx", pidx.toString())
        }
    }
}