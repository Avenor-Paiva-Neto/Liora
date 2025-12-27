package com.example.liora

// Esta é a ÚNICA definição da classe Question em todo o projeto.
data class Question(
    val id: Int,
    val text: String,
    val options: List<String>
)