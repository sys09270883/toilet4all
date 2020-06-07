package com.example.toilet4all

import java.io.Serializable

data class Post(val pidx: Long, val title: String, val content: String, val date: String, val name: String, val password: String)
    : Serializable{
    constructor(): this(-1, "none", "none", "none", "none", "none")
}