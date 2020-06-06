package com.example.toilet4all

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_board.*

class BoardActivity : AppCompatActivity() {

    lateinit var adapter: BoardAdapter
    var boardContentList = mutableListOf<Board>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        init()
    }

    private fun init() {
        /* test board */
        boardContentList.add(Board("첫 번째 글", "첫 번째 글의 내용입니다.", "2020-06-05", "신윤섭"
            , "1234"))
        boardContentList.add(Board("두 번째 글", "두 번째 글의 내용입니다.", "2020-06-05", "김영경"
            , "1234"))
        boardContentList.add(Board("세 번째 글", "세 번째 글의 내용입니다.", "2020-06-05", "김아지"
            , "1234"))
        boardContentList.add(Board("네 번째 글", "네 번째 글의 내용입니다.", "2020-06-05", "다현"
            , "1234"))
        boardContentList.add(Board("다섯 번째 글", "다섯 번째 글의 내용입니다.", "2020-06-05", "쯔위"
            , "1234"))
        boardContentList.add(Board("여섯 번째 글", "여섯 번째 글의 내용입니다.", "2020-06-05", "나연"
            , "1234"))
        boardContentList.add(Board("일곱 번째 글", "일곱 번째 글의 내용입니다.", "2020-06-05", "사나"
            , "1234"))
        boardContentList.add(Board("여덟 번째 글", "여덟 번째 글의 내용입니다.", "2020-06-05", "모모"
            , "1234"))
        /* end test */

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        adapter = BoardAdapter(boardContentList)
        adapter.itemClickListener = object : BoardAdapter.OnItemClickListener {
            override fun onItemClick(
                holder: BoardAdapter.ViewHolder,
                view: View,
                data: Board,
                position: Int
            ) {
                toast(position)
            }
        }
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, layoutManager.orientation))

        homeButton.setOnClickListener {
            onBackPressed()
        }
    }

    fun toast(position: Int) {
        Toast.makeText(this, "$position 클릭", Toast.LENGTH_SHORT).show()
    }
}