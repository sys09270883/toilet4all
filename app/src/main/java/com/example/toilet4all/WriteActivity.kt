package com.example.toilet4all

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
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
            if (nameEditTextView.text?.isEmpty()!!) {
                Toast.makeText(this, "아이디를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if (!isValid(nameEditTextView.text.toString())) {
                Toast.makeText(this, "아이디는 한글, 영문만 가능합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if (passwordEditTextView.text?.isEmpty()!!) {
                Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if (passwordEditTextView.text?.length!! > 15) {
                Toast.makeText(this, "비밀번호는 15자 이내입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
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

        nameEditTextView.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when {
                    s.toString().isEmpty() -> nameTextField.error = "아이디를 입력하세요."
                    isValid(s.toString()) -> nameTextField.error = null
                    else -> nameTextField.error = "아이디는 한글, 영문만 가능합니다."
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                when {
                    s.toString().isEmpty() -> nameTextField.error = "아이디를 입력하세요."
                }
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        )
    }
    
    private fun isValid(id: String): Boolean {
        if (id.isEmpty())
            return false
        val idRegex = Regex("^[가-힣0-9a-zA-Z\\s]+\$")
        val matchResult = idRegex.matches(id)
        if (matchResult)
            return true
        return false
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