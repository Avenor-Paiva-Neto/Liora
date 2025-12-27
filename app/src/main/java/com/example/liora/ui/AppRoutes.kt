package com.example.liora.ui

import androidx.navigation.NavType

object AppRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val NUMERO = "numero"
    const val NOME = "nome"
    const val SOBRE = "sobre"
    const val HOBBIES = "hobbies"
    const val LIMITES = "limites"
    const val PREFERENCIAS = "preferencias"
    const val PERSONALIDADE = "personalidade"
    const val IMAGEM = "imagem"
    const val BIO = "bio"
    const val TERMOS = "termos"

    // Rotas do App Principal (Mundo 2)
    const val MAIN_APP_HOST = "main_app_host"
    const val DISCOVERY = "discovery"
    const val LIKES = "likes"
    const val CHAT_LIST = "chat_list" // Rota para ConversationsScreen
    const val INDIVIDUAL_CHAT = "individual_chat" // Rota base para ChatScreen
    const val INDIVIDUAL_CHAT_ROUTE_WITH_ARGS = "$INDIVIDUAL_CHAT/{matchedUserId}" // Rota completa da ChatScreen com argumento
    const val PERFIL = "perfil"
}