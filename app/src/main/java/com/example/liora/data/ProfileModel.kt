package com.example.liora.data

data class Profile(
    val id: Int,
    val name: String,
    val age: Int,
    val distance: Int,
    val bio: String,
    val imageUrl: String,
    val tags: List<String>
)